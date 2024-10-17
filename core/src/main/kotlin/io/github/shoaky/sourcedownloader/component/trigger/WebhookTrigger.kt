package io.github.shoaky.sourcedownloader.component.trigger

import java.util.concurrent.Executors

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
            executor.submit(task)
        }
    }

    override fun stop() {
        adapter.unregisterEndpoint("/webhook/$path", method)
    }

    companion object {
        private val executor = Executors.newThreadPerTaskExecutor(
            Thread.ofVirtual().name("webhook-trigger", 0).factory()
        )
    }

    interface Adapter {

        fun registerEndpoint(path: String, method: String, handler: () -> Unit)
        fun unregisterEndpoint(path: String, method: String)

    }

}

