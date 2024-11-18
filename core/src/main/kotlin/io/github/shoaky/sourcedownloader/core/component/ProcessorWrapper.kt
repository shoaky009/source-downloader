package io.github.shoaky.sourcedownloader.core.component

import io.github.shoaky.sourcedownloader.core.processor.SourceProcessor

data class ProcessorWrapper(
    val name: String,
    val processor: SourceProcessor
) : ObjectWrapper<SourceProcessor> {

    override fun get(): SourceProcessor {
        return processor
    }
}