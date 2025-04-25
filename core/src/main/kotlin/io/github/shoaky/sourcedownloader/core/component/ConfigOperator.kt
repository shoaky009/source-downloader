package io.github.shoaky.sourcedownloader.core.component

import io.github.shoaky.sourcedownloader.core.ProcessorConfig
import io.github.shoaky.sourcedownloader.core.ProcessorConfigStorage
import io.github.shoaky.sourcedownloader.sdk.component.ComponentRootType

interface ConfigOperator : ProcessorConfigStorage, ComponentConfigStorage, InstanceConfigStorage {

    fun save(type: String, componentConfig: ComponentConfig)

    fun save(name: String, processorConfig: ProcessorConfig)

    fun deleteComponent(topType: ComponentRootType, type: String, name: String): Boolean

    fun deleteProcessor(name: String): Boolean
}