package xyz.shoaky.sourcedownloader.core

import xyz.shoaky.sourcedownloader.core.processor.SourceProcessor

interface ProcessorManager {

    fun createProcessor(config: ProcessorConfig): SourceProcessor

    fun getProcessor(name: String): SourceProcessor?

    fun getProcessors(): List<SourceProcessor>

    fun destroy(processorName: String)

    fun getAllProcessorNames(): Set<String>
}