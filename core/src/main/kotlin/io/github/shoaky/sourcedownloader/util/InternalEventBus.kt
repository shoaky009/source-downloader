package io.github.shoaky.sourcedownloader.util

import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

object InternalEventBus {

    private val listeners: MutableMap<String, MutableList<Consumer<*>>> = ConcurrentHashMap()
    private val listenerEventTypes: MutableMap<Consumer<*>, Class<*>> = ConcurrentHashMap()

    fun <T> register(address: String, type: Class<T>, listener: Consumer<T>) {
        listeners.computeIfAbsent(address) { ArrayList() }.add(listener)
        listenerEventTypes[listener] = type
    }

    fun <T : Any> post(address: String, event: T) {
        val consumers = listeners[address] ?: return
        for (listener in consumers) {
            val type = listenerEventTypes[listener] ?: continue
            if (type.isInstance(event).not()) {
                throw IllegalArgumentException(
                    "Listener ${listener.javaClass.name} is not compatible with event ${event::javaClass.name}"
                )
            }
            @Suppress("UNCHECKED_CAST")
            (listener as Consumer<T>).accept(event)
        }
    }

    fun unregister(address: String, listener: Consumer<*>) {
        val consumers = listeners[address] ?: return
        consumers.remove(listener)
        listenerEventTypes.remove(listener)
        if (consumers.isEmpty()) {
            listeners.remove(address)
        }
    }
}
