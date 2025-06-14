package io.github.shoaky.sourcedownloader.component.trigger


/**
 * Webhook触发器
 */
class WebhookTrigger(
    private val path: String,
    private val method: String = "GET",
    private val adapter: Adapter
) : HoldingTaskTrigger() {

    override fun start() {
        adapter.registerEndpoint("/webhook/$path", method) {
            endpoint()
        }
    }

    @Suppress("UNUSED")
    fun endpoint() {
        for (task in tasks) {
            Thread.ofVirtual().name("webhook-trigger-$path").start(task)
        }
    }

    override fun stop() {
        adapter.unregisterEndpoint("/webhook/$path", method)
    }

    interface Adapter {

        fun registerEndpoint(path: String, method: String, handler: () -> Unit)
        fun unregisterEndpoint(path: String, method: String)

    }

}

