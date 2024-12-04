package io.github.shoaky.sourcedownloader.application.vertx

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.zaxxer.hikari.HikariDataSource
import io.github.shoaky.sourcedownloader.CoreApplication
import io.github.shoaky.sourcedownloader.component.supplier.WebhookTriggerSupplier
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
import io.github.shoaky.sourcedownloader.sdk.util.AdaptabilityHandler
import io.github.shoaky.sourcedownloader.service.ComponentService
import io.github.shoaky.sourcedownloader.service.ProcessingContentService
import io.github.shoaky.sourcedownloader.service.ProcessorService
import io.github.shoaky.sourcedownloader.util.StopWatch
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.jackson.DatabindCodec
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.deploymentOptionsOf
import io.vertx.kotlin.core.vertxOptionsOf
import io.vertx.kotlin.micrometer.micrometerMetricsOptionsOf
import io.vertx.kotlin.micrometer.vertxJmxMetricsOptionsOf
import org.jetbrains.exposed.sql.Database

class SourceDownloaderVertxApplication {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            val stopWatch = StopWatch("SourceDownloaderApplication")
            stopWatch.start("application.config")
            val applicationConfig = ApplicationConfig()
            stopWatch.stop()

            val vertx = vertx()
            val context = createCoreApplication(args, applicationConfig, stopWatch, vertx)

            stopWatch.start("database")
            initDataSource(applicationConfig)
            stopWatch.stop()

            setupObjectMapper()
            stopWatch.start("http.server")
            vertx
                .deployVerticle(
                    HttpServerVerticle(applicationConfig, context), deploymentOptionsOf(
                        worker = true
                    )
                )
                .onComplete {
                    stopWatch.stop()
                    log.info("Application started in ${stopWatch.prettyPrint()}")
                }
        }

        private fun setupObjectMapper() {
            DatabindCodec.mapper()
                .registerKotlinModule()
                .registerModule(JavaTimeModule())
                .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .addHandler(AdaptabilityHandler)
        }

        private fun vertx(): Vertx {
            val options = vertxOptionsOf(
                metricsOptions = micrometerMetricsOptionsOf(
                    enabled = true,
                    jvmMetricsEnabled = true,
                    jmxMetricsOptions = vertxJmxMetricsOptionsOf(enabled = true),
                )
            )
            val vertx = Vertx.vertx(options)
            return vertx
        }

        private fun initDataSource(applicationConfig: ApplicationConfig) {
            val dataSource = HikariDataSource(applicationConfig.datasource)
            Database.connect(dataSource)
        }

        private fun createCoreApplication(
            args: Array<String>,
            applicationConfig: ApplicationConfig,
            stopWatch: StopWatch,
            vertx: Vertx
        ): ApplicationContext {
            stopWatch.start("core.application.base")
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

            val webhookRouter = Router.router(vertx)
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

            configOperator.init()
            stopWatch.stop()

            stopWatch.start("core.application.start")
            application.start()
            stopWatch.stop()
            return ApplicationContext(
                instanceManager,
                componentManager,
                processorManager,
                pluginManager,
                configOperator,
                props,
                ComponentService(componentManager, configOperator),
                ProcessorService(processorManager, configOperator, processingStorage),
                ProcessingContentService(processingStorage, processorManager),
                application,
                processingStorage,
                webhookRouter
            )
        }
    }
}

fun createRouteHandler(block: (ctx: RoutingContext) -> Any): Handler<RoutingContext> =
    Handler<RoutingContext> { ctx ->
        block.invoke(ctx)
    }

fun createRouteCoHandler(block: suspend (ctx: RoutingContext) -> Unit): suspend (RoutingContext) -> Unit {
    return { context: RoutingContext ->
        block.invoke(context)
    }
}