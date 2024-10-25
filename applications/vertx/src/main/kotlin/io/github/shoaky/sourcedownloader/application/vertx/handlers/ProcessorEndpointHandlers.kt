package io.github.shoaky.sourcedownloader.application.vertx.handlers

import io.github.shoaky.sourcedownloader.application.vertx.createRouteHandler
import io.github.shoaky.sourcedownloader.core.ProcessorConfig
import io.github.shoaky.sourcedownloader.core.processor.DryRunOptions
import io.github.shoaky.sourcedownloader.service.PageRequest
import io.github.shoaky.sourcedownloader.service.ProcessorService
import io.vertx.core.Handler
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.coroutineRouter

class ProcessorEndpointHandlers(
    private val processorService: ProcessorService
) {

    fun getProcessors(): Handler<RoutingContext> {
        return createRouteHandler { ctx ->
            val name = ctx.request().getParam("name")
            val pageNumber = ctx.request().getParam("pageNumber")?.toIntOrNull() ?: 0
            val pageSize = ctx.request().getParam("pageSize")?.toIntOrNull() ?: 50
            val data = processorService.getProcessors(name, PageRequest(pageNumber, pageSize))
            ctx.response().end(
                JsonArray(data).toBuffer()
            )
        }
    }

    fun getConfig(): Handler<RoutingContext> {
        return createRouteHandler { ctx ->
            val processorName = ctx.pathParam("processorName")
            val config = processorService.getConfig(processorName)
            ctx.response().end(
                JsonObject.mapFrom(config).toBuffer()
            )
        }
    }

    fun create(): Handler<RoutingContext> {
        return createRouteHandler { ctx ->
            val processorConfig = ctx.body().asJsonObject().mapTo(ProcessorConfig::class.java)
            processorService.create(processorConfig)
            ctx.response().setStatusCode(201).end()
        }
    }

    fun update(): Handler<RoutingContext> {
        return createRouteHandler { ctx ->
            val processorName = ctx.pathParam("processorName")
            val processorConfig = ctx.body().asJsonObject().mapTo(ProcessorConfig::class.java)
            val updatedConfig = processorService.update(processorName, processorConfig)
            ctx.response().end(
                JsonObject.mapFrom(updatedConfig).toBuffer()
            )
        }
    }

    fun delete(): Handler<RoutingContext> {
        return createRouteHandler { ctx ->
            val processorName = ctx.pathParam("processorName")
            processorService.delete(processorName)
            ctx.response().setStatusCode(204).end()
        }
    }

    fun reload(): Handler<RoutingContext> {
        return createRouteHandler { ctx ->
            val processorName = ctx.pathParam("processorName")
            processorService.reload(processorName)
            ctx.response().setStatusCode(204).end()
        }
    }

    fun dryRun(): Handler<RoutingContext> {
        return createRouteHandler { ctx ->
            val processorName = ctx.pathParam("processorName")
            val options = if (ctx.body().isEmpty) {
                DryRunOptions()
            } else {
                ctx.body().asJsonObject().mapTo(DryRunOptions::class.java)
            }
            val data = processorService.dryRun(processorName, options)
            ctx.response().end(
                JsonArray(data).toBuffer()
            )
        }
    }

    fun dryRunStream(): Handler<RoutingContext> {
        return createRouteHandler { ctx ->
            val processorName = ctx.pathParam("processorName")
            val options = if (ctx.body().isEmpty) {
                DryRunOptions()
            } else {
                ctx.body().asJsonObject().mapTo(DryRunOptions::class.java)
            }
            val streamData = processorService.dryRunStream(processorName, options)
            // 假设你有流式处理响应的逻辑
            ctx.response().setChunked(true)
            // streamData.collect { item ->
            //     ctx.response().write(JsonObject.mapFrom(item).toBuffer())
            // }.onComplete {
            //     ctx.response().end()
            // }
        }
    }

    fun trigger(): Handler<RoutingContext> {
        return createRouteHandler { ctx ->
            val processorName = ctx.pathParam("processorName")
            processorService.trigger(processorName)
            ctx.response().setStatusCode(202).end()
        }
    }

    fun rename(): Handler<RoutingContext> {
        return createRouteHandler { ctx ->
            val processorName = ctx.pathParam("processorName")
            processorService.rename(processorName)
            ctx.response().setStatusCode(202).end()
        }
    }

    // fun postItems(): Handler<RoutingContext> {
    //     return createRouteHandler { ctx ->
    //         val processorName = ctx.pathParam("processorName")
    //         val items = ctx.bodyAsJsonArray.map { it.mapTo(SourceItem::class.java) }
    //         processorService.postItems(processorName, items)
    //         ctx.response().setStatusCode(204).end()
    //     }
    // }

    fun getState(): Handler<RoutingContext> {
        return createRouteHandler { ctx ->
            val processorName = ctx.pathParam("processorName")
            val state = processorService.getState(processorName)
            ctx.response().end(
                JsonObject.mapFrom(state).toBuffer()
            )
        }
    }

    // fun modifyPointer(): Handler<RoutingContext> {
    //     return createRouteHandler { ctx ->
    //         val processorName = ctx.pathParam("processorName")
    //         val jsonPath = ctx.bodyAsJson.getString("jsonPath")
    //         processorService.modifyPointer(processorName, jsonPath)
    //         ctx.response().setStatusCode(204).end()
    //     }
    // }

    fun enableProcessor(): Handler<RoutingContext> {
        return createRouteHandler { ctx ->
            val processorName = ctx.pathParam("processorName")
            processorService.enableProcessor(processorName)
            ctx.response().setStatusCode(204).end()
        }
    }

    fun disableProcessor(): Handler<RoutingContext> {
        return createRouteHandler { ctx ->
            val processorName = ctx.pathParam("processorName")
            processorService.disableProcessor(processorName)
            ctx.response().setStatusCode(204).end()
        }
    }
}

class HttpServerVerticle : CoroutineVerticle() {

    override suspend fun start() {
        coroutineRouter {
            this.coroutineContext
        }
    }

    override suspend fun stop() {
        // ...
    }
}