package xyz.shoaky.sourcedownloader.util

import com.google.common.eventbus.EventBus

object Events {
    private val bus: EventBus = EventBus()
    fun register(any: Any) {
        bus.register(any)
    }

    fun post(event: Any) {
        bus.post(event)
    }
}