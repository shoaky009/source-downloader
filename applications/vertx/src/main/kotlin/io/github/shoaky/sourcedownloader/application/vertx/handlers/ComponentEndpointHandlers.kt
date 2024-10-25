package io.github.shoaky.sourcedownloader.application.vertx.handlers

import io.github.shoaky.sourcedownloader.application.vertx.createRouteHandler
import io.github.shoaky.sourcedownloader.sdk.component.ComponentTopType
import io.github.shoaky.sourcedownloader.service.ComponentCreateBody
import io.github.shoaky.sourcedownloader.service.ComponentService
import io.vertx.core.Handler
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext

class ComponentEndpointHandlers(
    private val componentService: ComponentService
) {

    fun queryComponents(): Handler<RoutingContext> {
        return createRouteHandler { ctx ->
            val request = ctx.request()
            val type = request.getParam("type")?.let { ComponentTopType.valueOf(it.uppercase()) }
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
            val type: ComponentTopType = ctx.pathParams().getValue("type").let {
                ComponentTopType.valueOf(it.uppercase())
            }
            val typeName = ctx.pathParams().getValue("typeName")
            val name: String = ctx.pathParams().getValue("name")
            val pathParams = ctx.pathParams()
            println(pathParams)
            componentService.deleteComponent(type, typeName, name)
            ctx.response().statusCode = 204
            ctx.response().end()
        }
    }

    fun reload(): Handler<RoutingContext> {
        return createRouteHandler { ctx ->
            val type: ComponentTopType = ctx.pathParams().getValue("type").let {
                ComponentTopType.valueOf(it.uppercase())
            }
            val typeName = ctx.pathParams().getValue("typeName")
            val name: String = ctx.pathParams().getValue("name")
            componentService.reload(type, typeName, name)
            ctx.response().statusCode = 204
            ctx.response().end()
        }
    }

    // @GetMapping("/state-stream")
    // suspend fun stateDetailStream(
    //     @RequestParam id: MutableSet<String>,
    // ): Flow<ServerSentEvent<Any>> {
    //     return componentService.stateDetailStream(id).map {
    //         ServerSentEvent.builder(it.data).event(it.event).id(it.id).build()
    //     }
    // }

    fun getTypes(): Handler<RoutingContext> {
        return createRouteHandler { ctx ->
            val type = ctx.request().getParam("type")?.let { ComponentTopType.valueOf(it.uppercase()) }
            componentService.getTypes(type)
            ctx.response().end()
        }
    }

    fun getSchema(): Handler<RoutingContext> {
        return createRouteHandler { ctx ->
            val type = ctx.request().getParam("type").let { ComponentTopType.valueOf(it.uppercase()) }
            val typeName = ctx.request().getParam("typeName")
            val data = componentService.getSchema(type, typeName)
            ctx.response().end(
                JsonObject.mapFrom(data).toBuffer()
            )
        }
    }

}