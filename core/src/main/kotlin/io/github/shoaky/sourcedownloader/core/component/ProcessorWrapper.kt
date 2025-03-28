package io.github.shoaky.sourcedownloader.core.component

import io.github.shoaky.sourcedownloader.core.processor.SourceProcessor

data class ProcessorWrapper(
    val name: String,
    val processor: SourceProcessor?,
    val errorMessage: String?
) : ObjectWrapper<SourceProcessor> {

    override fun get(): SourceProcessor {
        if (processor != null) {
            return processor
        }
        throw RuntimeException("$name is not a valid processor, error: $errorMessage")
    }

    override fun type(): Class<*> {
        return SourceProcessor::class.java
    }
}