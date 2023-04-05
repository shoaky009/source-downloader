package xyz.shoaky.sourcedownloader.core

interface ProcessorManager {

    fun createProcessor(config: ProcessorConfig): SourceProcessor

    fun getProcessor(name: String): SourceProcessor?
}