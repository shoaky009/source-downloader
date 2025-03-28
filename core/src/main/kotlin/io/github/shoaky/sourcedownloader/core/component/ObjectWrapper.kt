package io.github.shoaky.sourcedownloader.core.component

interface ObjectWrapper<T : Any> {

    fun get(): T

    fun type(): Class<*>
}