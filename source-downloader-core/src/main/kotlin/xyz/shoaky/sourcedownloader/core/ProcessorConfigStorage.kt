package xyz.shoaky.sourcedownloader.core

import xyz.shoaky.sourcedownloader.core.config.ProcessorConfig

interface ProcessorConfigStorage {

    fun getAllProcessorConfig(): List<ProcessorConfig>
}