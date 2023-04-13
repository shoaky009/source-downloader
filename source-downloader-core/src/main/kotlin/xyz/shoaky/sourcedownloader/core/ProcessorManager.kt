package xyz.shoaky.sourcedownloader.core

import xyz.shoaky.sourcedownloader.core.config.ProcessorConfig

interface ProcessorManager {

    fun createProcessor(config: ProcessorConfig): SourceProcessor

    fun getProcessor(name: String): SourceProcessor?
}