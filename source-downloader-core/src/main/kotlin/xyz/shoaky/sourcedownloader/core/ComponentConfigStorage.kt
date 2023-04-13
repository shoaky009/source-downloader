package xyz.shoaky.sourcedownloader.core

import xyz.shoaky.sourcedownloader.core.config.ComponentConfig
import xyz.shoaky.sourcedownloader.core.config.ProcessorConfig

interface ComponentConfigStorage {

    fun getAllComponentConfig(): Map<String, List<ComponentConfig>>
}

interface ConfigOperator {

    fun save(type: String, componentConfig: ComponentConfig)

    fun save(name: String, processorConfig: ProcessorConfig)

    fun delete(type: String, componentConfig: ComponentConfig)
    fun delete(name: String, processorConfig: ProcessorConfig)
}