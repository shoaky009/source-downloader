package xyz.shoaky.sourcedownloader.core

import xyz.shoaky.sourcedownloader.sdk.component.Components

interface ComponentConfigStorage {

    fun getAllComponentConfig(): Map<String, List<ComponentConfig>>
}

interface ConfigOperator {

    fun save(type: String, componentConfig: ComponentConfig)

    fun save(name: String, processorConfig: ProcessorConfig)

    fun deleteComponent(topType: Components, type: String, name: String)
    fun deleteProcessor(name: String)
}