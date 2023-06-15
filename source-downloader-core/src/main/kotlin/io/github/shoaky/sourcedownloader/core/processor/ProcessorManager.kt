package io.github.shoaky.sourcedownloader.core.processor

import io.github.shoaky.sourcedownloader.core.ProcessorConfig

interface ProcessorManager {

    fun createProcessor(config: ProcessorConfig): SourceProcessor

    fun getProcessor(name: String): SourceProcessor?

    fun getProcessors(): List<SourceProcessor>

    fun destroy(processorName: String)

    fun getAllProcessorNames(): Set<String>
}