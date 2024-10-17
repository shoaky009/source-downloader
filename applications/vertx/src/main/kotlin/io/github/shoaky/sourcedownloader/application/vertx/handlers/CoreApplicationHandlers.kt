package io.github.shoaky.sourcedownloader.application.vertx.handlers

import io.github.shoaky.sourcedownloader.CoreApplication
import io.github.shoaky.sourcedownloader.application.vertx.createRouteHandler
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext

class CoreApplicationHandlers(
    private val coreApplication: CoreApplication
) {

    fun reload(): Handler<RoutingContext> {
        return createRouteHandler {
            coreApplication.reload()
            it.response().statusCode = 204
            it.response().end()
        }
    }

}