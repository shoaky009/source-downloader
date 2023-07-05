package io.github.shoaky.sourcedownloader.core.processor

import io.github.shoaky.sourcedownloader.core.ProcessorConfig
import io.github.shoaky.sourcedownloader.core.component.ProcessorWrapper

interface ProcessorManager {

    fun createProcessor(config: ProcessorConfig): ProcessorWrapper

    fun getProcessor(name: String): ProcessorWrapper?

    fun getProcessors(): List<ProcessorWrapper>

    fun destroy(processorName: String)

    fun getAllProcessorNames(): Set<String>
}