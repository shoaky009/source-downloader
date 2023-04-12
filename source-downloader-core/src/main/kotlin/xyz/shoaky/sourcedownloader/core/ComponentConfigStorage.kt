package xyz.shoaky.sourcedownloader.core

import xyz.shoaky.sourcedownloader.core.config.ComponentConfig

interface ComponentConfigStorage {

    fun getAllConfig(): Map<String, List<ComponentConfig>>
}

interface ConfigOperator {

    fun save(type: String, config: ComponentConfig)

    fun save(name: String, config: ProcessorConfig)

    fun delete(type: String, config: ComponentConfig)
    fun delete(name: String, config: ProcessorConfig)
}