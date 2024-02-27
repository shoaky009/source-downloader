package io.github.shoaky.sourcedownloader.core

import io.github.shoaky.sourcedownloader.core.component.ComponentFailureType
import io.github.shoaky.sourcedownloader.throwComponentException

interface ProcessorConfigStorage {

    fun getAllProcessorConfig(): List<ProcessorConfig>

    fun getProcessorConfig(name: String): ProcessorConfig {
        return getAllProcessorConfig().firstOrNull { it.name == name }
            ?: throwComponentException("No processor config found for $name", ComponentFailureType.PROCESSOR_NOT_FOUND)
    }
}