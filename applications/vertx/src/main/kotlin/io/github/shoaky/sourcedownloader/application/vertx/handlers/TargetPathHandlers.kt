package io.github.shoaky.sourcedownloader.application.vertx.handlers

import io.github.shoaky.sourcedownloader.application.vertx.createRouteHandler
import io.github.shoaky.sourcedownloader.core.ProcessingStorage
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext

class TargetPathHandlers(
    private val processingStorage: ProcessingStorage
) {

    fun deleteTargetPaths(): Handler<RoutingContext> {
        return createRouteHandler { ctx ->
            ctx.request().bodyHandler { buffer ->
                val paths = buffer.toJsonArray().map { it.toString() }
                processingStorage.deleteTargetPaths(paths, null)
                ctx.response().statusCode = 204
                ctx.response().end()
            }
        }
    }
}