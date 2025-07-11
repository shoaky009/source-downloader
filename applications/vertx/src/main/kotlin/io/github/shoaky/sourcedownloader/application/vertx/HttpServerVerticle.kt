package io.github.shoaky.sourcedownloader.application.vertx

import io.github.shoaky.sourcedownloader.application.vertx.handlers.*
import io.github.shoaky.sourcedownloader.core.processor.log
import io.github.shoaky.sourcedownloader.sdk.http.StatusCodes
import io.github.shoaky.sourcedownloader.service.EventItem
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpServer
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.coroutineRouter
import io.vertx.micrometer.MetricsService

class HttpServerVerticle(
    private val applicationConfig: ApplicationConfig,
    private val context: ApplicationContext
) : CoroutineVerticle() {

    private var httpServer: HttpServer? = null

    override suspend fun start() {
        val router = Router.router(vertx)
            // TODO 调整status code
            .errorHandler(StatusCodes.INTERNAL_SERVER_ERROR, ProblemDetailFailureHandler)
        router
            .route().subRouter(context.webhookRouter)
        registerRouters(router, vertx, context)
        vertx.createHttpServer(applicationConfig.server)
            .requestHandler(router)
            .listen()
            .onComplete {
                if (it.succeeded()) {
                    httpServer = it.result()
                    log.info("HTTP Server started on port ${it.result().actualPort()}")
                } else {
                    log.error("Failed to start server", it.cause())
                }
            }
    }

    override suspend fun stop() {
        httpServer?.close()
    }

    private fun registerRouters(router: Router, vertx: Vertx, context: ApplicationContext) {
        router
            .route(HttpMethod.GET, "/api/application/reload")
            .handler(CoreApplicationHandlers(context.coreApplication).reload())

        val cpHandlers = ComponentEndpointHandlers(context.componentService)
        router.route(HttpMethod.GET, "/api/component")
            .handler(cpHandlers.queryComponents())
        router.route(HttpMethod.POST, "/api/component")
            .handler(cpHandlers.createComponent())
        router.route(HttpMethod.DELETE, "/api/component/:type/:typeName/:name")
            .handler(cpHandlers.deleteComponent())
        router.route(HttpMethod.GET, "/api/component/:type/:typeName/:name/reload")
            .handler(cpHandlers.reload())

        coroutineRouter {
            router.get("/api/component/state-stream")
                .coHandler(requestHandler = cpHandlers.stateDetailStream())
        }

        router.route(HttpMethod.GET, "/api/component/types")
            .handler(cpHandlers.getTypes())
        router.route(HttpMethod.GET, "/api/component/:type/:typeName/metadata")
            .handler(cpHandlers.getSchema())

        val processorHandlers = ProcessorEndpointHandlers(context.processorService)
        router.route(HttpMethod.GET, "/api/processor")
            .handler(processorHandlers.getProcessors())
        router.route(HttpMethod.GET, "/api/processor/:processorName")
            .handler(processorHandlers.getConfig())
        router.route(HttpMethod.POST, "/api/processor")
            .handler(processorHandlers.create())
        router.route(HttpMethod.PUT, "/api/processor/:processorName")
            .handler(processorHandlers.update())
        router.route(HttpMethod.DELETE, "/api/processor/:processorName")
            .handler(processorHandlers.delete())
        router.route(HttpMethod.GET, "/api/processor/:processorName/reload")
            .handler(processorHandlers.reload())
        router.route(HttpMethod.GET, "/api/processor/:processorName/dry-run")
            .handler(processorHandlers.dryRun())
        router.route(HttpMethod.POST, "/api/processor/:processorName/dry-run")
            .handler(processorHandlers.dryRun())

        coroutineRouter {
            val dryRunStreamHandler = processorHandlers.dryRunStream()
            router.route(HttpMethod.POST, "/api/processor/:processorName/dry-run-stream")
                .coHandler(requestHandler = dryRunStreamHandler)
            router.route(HttpMethod.GET, "/api/processor/:processorName/dry-run-stream")
                .coHandler(requestHandler = dryRunStreamHandler)
        }

        router.route(HttpMethod.GET, "/api/processor/:processorName/trigger")
            .handler(processorHandlers.trigger())
        router.route(HttpMethod.GET, "/api/processor/:processorName/rename")
            .handler(processorHandlers.rename())
        router.route(HttpMethod.GET, "/api/processor/:processorName/state")
            .handler(processorHandlers.getState())

        val processingContentHandlers = ProcessingContentHandlers(
            context.processingContentService
        )
        router.route(HttpMethod.GET, "/api/processing-content/:id")
            .handler(processingContentHandlers.getProcessingContent())
        router.route(HttpMethod.GET, "/api/processing-content")
            .handler(processingContentHandlers.queryContents())
        router.route(HttpMethod.PUT, "/api/processing-content/:id")
            .handler(processingContentHandlers.modifyProcessingContent())
        router.route(HttpMethod.DELETE, "/api/processing-content/:id")
            .handler(processingContentHandlers.deleteProcessingContent())
        router.route(HttpMethod.POST, "/api/processing-content/:id/reprocess")
            .handler(processingContentHandlers.reprocess())

        val targetPathHandlers = TargetPathHandlers(context.processingStorage)
        router.route(HttpMethod.DELETE, "/api/target-path")
            .handler(targetPathHandlers.deleteTargetPaths())

        val metricsService = MetricsService.create(vertx)
        router.route("/metrics")
            .handler {
                val names = it.request().params().getAll("name")
                if (names.isEmpty()) {
                    it.response().end(metricsService.metricsSnapshot.toBuffer())
                    return@handler
                }
                val result = JsonObject()
                for (name in names) {
                    val metric = metricsService.getMetricsSnapshot(name)
                    if (metric != null) {
                        result.mergeIn(metric)
                    }
                }
                it.response().end(result.toBuffer())
            }
        // 优先级最低路由ui页面
        router.routeWithRegex(HttpMethod.GET, "^(?!\\/api).*")
            .handler(StaticHandler.create("static"))
            .handler {
                it.reroute("/")
            }
    }

}

fun dataToJsonString(it: EventItem): String? {
    val raw = it.data
    val data = if (raw is Iterable<*>) {
        JsonArray(raw.toList()).encode()
    } else {
        runCatching {
            JsonObject.mapFrom(it.data).encode()
        }.getOrDefault(raw.toString())
    }
    return data
}