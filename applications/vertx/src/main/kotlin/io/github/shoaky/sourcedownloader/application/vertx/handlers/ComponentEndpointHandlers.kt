package io.github.shoaky.sourcedownloader.application.vertx.handlers

import io.github.shoaky.sourcedownloader.application.vertx.createRouteCoHandler
import io.github.shoaky.sourcedownloader.application.vertx.createRouteHandler
import io.github.shoaky.sourcedownloader.application.vertx.dataToJsonString
import io.github.shoaky.sourcedownloader.core.processor.log
import io.github.shoaky.sourcedownloader.sdk.component.ComponentRootType
import io.github.shoaky.sourcedownloader.service.ComponentCreateBody
import io.github.shoaky.sourcedownloader.service.ComponentService
import io.vertx.core.Handler
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.onStart
import kotlin.coroutines.coroutineContext

class ComponentEndpointHandlers(
    private val componentService: ComponentService
) {

    fun queryComponents(): Handler<RoutingContext> {
        return createRouteHandler { ctx ->
            val request = ctx.request()
            val type = request.getParam("type")?.let { ComponentRootType.valueOf(it.uppercase()) }
            val typeName = request.getParam("typeName")
            val name = request.getParam("name")
            val data = componentService.queryComponents(type, typeName, name)
            ctx.response().end(
                JsonArray(data).toBuffer()
            )
        }
    }

    fun createComponent(): Handler<RoutingContext> {
        return createRouteHandler { ctx ->
            ctx.request().bodyHandler { buff ->
                val body = buff.toJsonObject().mapTo(ComponentCreateBody::class.java)
                componentService.createComponent(body)
            }
            ctx.response().statusCode = 201
            ctx.response().end()
        }
    }

    fun deleteComponent(): Handler<RoutingContext> {
        return createRouteHandler { ctx ->
            val type: ComponentRootType = ctx.pathParams().getValue("type").let {
                ComponentRootType.valueOf(it.uppercase())
            }
            val typeName = ctx.pathParams().getValue("typeName")
            val name: String = ctx.pathParams().getValue("name")
            componentService.deleteComponent(type, typeName, name)
            ctx.response().statusCode = 204
            ctx.response().end()
        }
    }

    fun reload(): Handler<RoutingContext> {
        return createRouteHandler { ctx ->
            val type: ComponentRootType = ctx.pathParams().getValue("type").let {
                ComponentRootType.valueOf(it.uppercase())
            }
            val typeName = ctx.pathParams().getValue("typeName")
            val name: String = ctx.pathParams().getValue("name")
            componentService.reload(type, typeName, name)
            ctx.response().statusCode = 204
            ctx.response().end()
        }
    }

    fun stateDetailStream(): suspend (RoutingContext) -> Unit {
        return createRouteCoHandler { ctx ->
            val coroutineContext = coroutineContext
            val id = ctx.request().params().getAll("id").toSet()
            val response = ctx.response()
                .closeHandler {
                    log.debug("Close state stream id:{}", id)
                    coroutineContext.cancelChildren()
                }

            response.headers()
                .add("Content-Type", "text/event-stream")
                .add("Cache-Control", "no-cache")
                .add("Connection", "keep-alive")
            response.setStatusCode(200)
                .setChunked(true)
                .write("")

            componentService.stateDetailStream(id)
                .onStart {
                    log.debug("Start state stream with id: {}", id)
                }
                .collect {
                    val data = dataToJsonString(it)
                    response.write("id:${it.id}\nevent:${it.event}\ndata:$data\n\n")
                }
        }

    }

    fun getTypes(): Handler<RoutingContext> {
        return createRouteHandler { ctx ->
            val type = ctx.request().getParam("type")?.let { ComponentRootType.valueOf(it.uppercase()) }
            componentService.getTypes(type)
            ctx.response().end()
        }
    }

    fun getSchema(): Handler<RoutingContext> {
        return createRouteHandler { ctx ->
            val type = ctx.request().getParam("type").let { ComponentRootType.valueOf(it.uppercase()) }
            val typeName = ctx.request().getParam("typeName")
            val data = componentService.getSchema(type, typeName)
            ctx.response().end(
                JsonObject.mapFrom(data).toBuffer()
            )
        }
    }

}