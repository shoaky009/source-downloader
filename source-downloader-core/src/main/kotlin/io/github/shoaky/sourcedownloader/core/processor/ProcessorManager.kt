package io.github.shoaky.sourcedownloader.core.processor

import io.github.shoaky.sourcedownloader.core.ProcessorConfig
import io.github.shoaky.sourcedownloader.core.component.ProcessorWrapper

interface ProcessorManager {

    fun createProcessor(config: ProcessorConfig)

    fun getProcessor(name: String): ProcessorWrapper

    fun exists(name: String): Boolean {
        return getAllProcessorNames().contains(name)
    }

    fun getProcessors(): List<ProcessorWrapper>

    fun destroy(processorName: String)

    fun getAllProcessorNames(): Set<String>
}