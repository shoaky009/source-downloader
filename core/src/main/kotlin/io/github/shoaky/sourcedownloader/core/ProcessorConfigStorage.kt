package io.github.shoaky.sourcedownloader.core

import io.github.shoaky.sourcedownloader.sdk.component.ComponentException

interface ProcessorConfigStorage {

    fun getAllProcessorConfig(): List<ProcessorConfig>

    fun getProcessorConfig(name: String): ProcessorConfig {
        return getAllProcessorConfig().firstOrNull { it.name == name }
            ?: throw ComponentException.processorMissing("No processor config found for $name")
    }
}