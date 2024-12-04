package io.github.shoaky.sourcedownloader.application.vertx

import io.github.shoaky.sourcedownloader.component.trigger.WebhookTrigger
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Router

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