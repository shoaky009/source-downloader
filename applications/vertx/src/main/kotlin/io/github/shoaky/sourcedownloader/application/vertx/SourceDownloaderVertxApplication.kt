package io.github.shoaky.sourcedownloader.application.vertx

import com.zaxxer.hikari.HikariDataSource
import io.github.shoaky.sourcedownloader.CoreApplication
import io.github.shoaky.sourcedownloader.application.vertx.handlers.*
import io.github.shoaky.sourcedownloader.component.supplier.WebhookTriggerSupplier
import io.github.shoaky.sourcedownloader.component.trigger.WebhookTrigger
import io.github.shoaky.sourcedownloader.config.SourceDownloaderProperties
import io.github.shoaky.sourcedownloader.core.PluginManager
import io.github.shoaky.sourcedownloader.core.ProcessingStorage
import io.github.shoaky.sourcedownloader.core.SimpleObjectWrapperContainer
import io.github.shoaky.sourcedownloader.core.YamlConfigOperator
import io.github.shoaky.sourcedownloader.core.component.ComponentManager
import io.github.shoaky.sourcedownloader.core.component.DefaultComponentManager
import io.github.shoaky.sourcedownloader.core.component.DefaultComponents
import io.github.shoaky.sourcedownloader.core.component.DefaultInstanceManager
import io.github.shoaky.sourcedownloader.core.processor.DefaultProcessorManager
import io.github.shoaky.sourcedownloader.core.processor.ProcessorManager
import io.github.shoaky.sourcedownloader.core.processor.log
import io.github.shoaky.sourcedownloader.repo.exposed.ExposedProcessingStorage
import io.github.shoaky.sourcedownloader.sdk.http.StatusCodes
import io.github.shoaky.sourcedownloader.service.ComponentService
import io.github.shoaky.sourcedownloader.service.ProcessingContentService
import io.github.shoaky.sourcedownloader.service.ProcessorService
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.kotlin.core.vertxOptionsOf
import io.vertx.kotlin.micrometer.micrometerMetricsOptionsOf
import io.vertx.micrometer.PrometheusScrapingHandler
import org.jetbrains.exposed.sql.Database

class SourceDownloaderVertxApplication {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            val applicationConfig = ApplicationConfig()
            val context = createCoreApplication(args, applicationConfig)

            SimpleMeterRegistry()
            val options = vertxOptionsOf(
                metricsOptions = micrometerMetricsOptionsOf(
                    enabled = true,
                    jvmMetricsEnabled = true,
                    micrometerRegistry = SimpleMeterRegistry()
                )
            )
            val vertx = Vertx.vertx(options)
            val router = Router.router(vertx)
                // handle unhandled exception
                .errorHandler(StatusCodes.INTERNAL_SERVER_ERROR, ProblemDetailFailureHandler)
            router
                .route().subRouter(context.webhookRouter)
            registerRouters(router, vertx, context)
            initDataSource(applicationConfig)
            vertx.createHttpServer(applicationConfig.server)
                .requestHandler(router)
                .listen()
                .onComplete {
                    if (it.succeeded()) {
                        log.info("Server started on port ${it.result().actualPort()}")
                    } else {
                        log.error("Failed to start server", it.cause())
                    }
                }
        }

        private fun initDataSource(applicationConfig: ApplicationConfig) {
            Database.connect(HikariDataSource(applicationConfig.datasource))
        }

        private fun registerRouters(router: Router, vertx: Vertx, context: ApplicationContext) {
            router
                .route(HttpMethod.GET, "/api/application/reload")
                .blockingHandler(CoreApplicationHandlers(context.coreApplication).reload())

            val cpHandlers = ComponentEndpointHandlers(context.componentService)
            router.route(HttpMethod.GET, "/api/component")
                .blockingHandler(cpHandlers.queryComponents())
            router.route(HttpMethod.POST, "/api/component")
                .blockingHandler(cpHandlers.createComponent())
            router.route(HttpMethod.DELETE, "/api/component/:type/:typeName/:name")
                .blockingHandler(cpHandlers.deleteComponent())
            router.route(HttpMethod.GET, "/api/component/:type/:typeName/:name/reload")
                .blockingHandler(cpHandlers.reload())
            router.route(HttpMethod.GET, "/api/component/types")
                .blockingHandler(cpHandlers.getTypes())
            router.route(HttpMethod.GET, "/api/component/:type/:typeName/metadata")
                .blockingHandler(cpHandlers.getSchema())

            val processorHandlers = ProcessorEndpointHandlers(context.processorService)
            router.route(HttpMethod.GET, "/api/processor")
                .blockingHandler(processorHandlers.getProcessors())
            router.route(HttpMethod.GET, "/api/processor/:processorName")
                .blockingHandler(processorHandlers.getConfig())
            router.route(HttpMethod.POST, "/api/processor")
                .blockingHandler(processorHandlers.create())
            router.route(HttpMethod.PUT, "/api/processor/:processorName")
                .blockingHandler(processorHandlers.update())
            router.route(HttpMethod.DELETE, "/api/processor/:processorName")
                .blockingHandler(processorHandlers.delete())
            router.route(HttpMethod.GET, "/api/processor/:processorName/reload")
                .blockingHandler(processorHandlers.reload())
            router.route(HttpMethod.GET, "/api/processor/:processorName/dry-run")
                .blockingHandler(processorHandlers.dryRun())
            router.route(HttpMethod.POST, "/api/processor/:processorName/dry-run")
                .blockingHandler(processorHandlers.dryRun())
            router.route(HttpMethod.GET, "/api/processor/:processorName/trigger")
                .blockingHandler(processorHandlers.trigger())
            router.route(HttpMethod.GET, "/api/processor/:processorName/rename")
                .blockingHandler(processorHandlers.rename())
            router.route(HttpMethod.GET, "/api/processor/:processorName/state")
                .blockingHandler(processorHandlers.getState())

            val processingContentHandlers = ProcessingContentHandlers(
                context.processingContentService
            )
            router.route(HttpMethod.GET, "/api/processing-content/:id")
                .blockingHandler(processingContentHandlers.getProcessingContent())
            router.route(HttpMethod.GET, "/api/processing-content")
                .blockingHandler(processingContentHandlers.queryContents())
            router.route(HttpMethod.PUT, "/api/processing-content/:id")
                .blockingHandler(processingContentHandlers.modifyProcessingContent())
            router.route(HttpMethod.DELETE, "/api/processing-content/:id")
                .blockingHandler(processingContentHandlers.deleteProcessingContent())
            router.route(HttpMethod.POST, "/api/processing-content/:id/reprocess")
                .blockingHandler(processingContentHandlers.reprocess())

            val targetPathHandlers = TargetPathHandlers(context.processingStorage)
            router.route(HttpMethod.DELETE, "/api/target-path")
                .blockingHandler(targetPathHandlers.deleteTargetPaths())
            router.route("/metrics")
                .handler(PrometheusScrapingHandler.create())

            // 优先级最低路由ui页面
            router.route("/*")
                .handler(StaticHandler.create("static"))
                .handler {
                    it.reroute("/")
                }
        }

        private fun createCoreApplication(
            args: Array<String>,
            applicationConfig: ApplicationConfig,
        ): ApplicationContext {
            val dataLocation = applicationConfig.sourceDownloader.dataLocation
            val configPath = dataLocation.resolve("config.yaml")
            val configOperator = YamlConfigOperator(configPath)

            val props = SourceDownloaderProperties(dataLocation)
            val container = SimpleObjectWrapperContainer()
            val processingStorage: ProcessingStorage = ExposedProcessingStorage()
            val instanceManager = DefaultInstanceManager(configOperator)
            val componentManager: ComponentManager = DefaultComponentManager(
                container,
                listOf(
                    DefaultComponents(),
                    configOperator
                )
            )
            val pluginManager = PluginManager(
                componentManager,
                instanceManager,
                props
            )
            val processorManager: ProcessorManager =
                DefaultProcessorManager(processingStorage, componentManager, container)

            val webhookRouter = Router.router(Vertx.vertx())
            val application = CoreApplication(
                props,
                DefaultInstanceManager(configOperator),
                componentManager,
                processorManager,
                pluginManager,
                listOf(configOperator),
                listOf(
                    WebhookTriggerSupplier(VertxWebhookAdapter(webhookRouter))
                )
            )

            val storage = ExposedProcessingStorage()
            application.start()
            return ApplicationContext(
                instanceManager,
                componentManager,
                processorManager,
                pluginManager,
                configOperator,
                props,
                ComponentService(componentManager, configOperator),
                ProcessorService(processorManager, configOperator, processingStorage),
                ProcessingContentService(storage, processorManager),
                application,
                storage,
                webhookRouter
            )
        }
    }
}

fun createRouteHandler(block: (ctx: RoutingContext) -> Any): Handler<RoutingContext> =
    Handler<RoutingContext> { ctx ->
        block.invoke(ctx)
    }

class VertxWebhookAdapter(
    private val router: Router
) : WebhookTrigger.Adapter {

    override fun registerEndpoint(path: String, method: String, handler: () -> Unit) {
        router.route(HttpMethod.valueOf(method), path)
            .blockingHandler(createRouteHandler { handler.invoke() })
    }

    override fun unregisterEndpoint(path: String, method: String) {
        router.route(HttpMethod.valueOf(method), path)
            .remove()
    }


}