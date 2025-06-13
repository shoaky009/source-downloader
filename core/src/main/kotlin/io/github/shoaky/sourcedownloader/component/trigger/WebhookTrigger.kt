package io.github.shoaky.sourcedownloader.component.trigger

import io.github.shoaky.sourcedownloader.core.processor.SourceProcessor

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
            SourceProcessor.processExecutor.execute(task)
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

