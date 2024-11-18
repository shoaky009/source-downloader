package io.github.shoaky.sourcedownloader.core.component

import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.SdComponent

data class ComponentWrapper<T : SdComponent>(
    val type: ComponentType,
    val name: String,
    val props: Properties,
    val component: T,
    val primary: Boolean = true
) : ObjectWrapper<T> {

    private val processorRef = mutableSetOf<String>()

    override fun get(): T {
        return component
    }

    fun getAndMarkRef(ref: String): T {
        addRef(ref)
        return component
    }

    fun addRef(ref: String) {
        processorRef.add(ref)
    }

    fun removeRef(ref: String) {
        processorRef.remove(ref)
    }

    fun getRefs(): Set<String> {
        return processorRef
    }

    fun componentName(): String {
        return "${type.typeName}:$name"
    }

}

