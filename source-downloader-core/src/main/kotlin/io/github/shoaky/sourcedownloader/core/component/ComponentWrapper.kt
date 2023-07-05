package io.github.shoaky.sourcedownloader.core.component

import io.github.shoaky.sourcedownloader.core.processor.SourceProcessor
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.SdComponent

data class ComponentWrapper<T : SdComponent>(
    val type: ComponentType,
    val name: String,
    val props: Properties,
    val component: T
) : ObjectWrapper<T> {
    override fun get(): T {
        return component
    }

}

data class ProcessorWrapper(
    val name: String,
    val processor: SourceProcessor
) : ObjectWrapper<SourceProcessor> {

    override fun get(): SourceProcessor {
        return processor
    }
}

interface ObjectWrapper<T : Any> {
    fun get(): T
}