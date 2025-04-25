package io.github.shoaky.sourcedownloader.core

import io.github.shoaky.sourcedownloader.core.component.ComponentConfig
import io.github.shoaky.sourcedownloader.core.component.ConfigOperator
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentRootType

class MemoryConfigOperator : ConfigOperator {

    private val components: MutableMap<String, MutableList<ComponentConfig>> = mutableMapOf()
    private val instances: MutableMap<String, Properties> = mutableMapOf()
    private val processors: MutableMap<String, ProcessorConfig> = mutableMapOf()

    override fun save(type: String, componentConfig: ComponentConfig) {
        val configs = components.getOrPut(type) { mutableListOf() }
        val index = configs.indexOfFirst { it.name == componentConfig.name }
        if (index < 0) {
            configs.add(componentConfig)
        } else {
            configs[index] = componentConfig
        }
    }

    override fun save(name: String, processorConfig: ProcessorConfig) {
        processors[name] = processorConfig
    }

    override fun deleteComponent(topType: ComponentRootType, type: String, name: String): Boolean {
        val configs = components[topType.primaryName]
        if (configs != null) {
            val removed = configs.filter { it.type == type && it.name == name }
            if (removed.isNotEmpty()) {
                configs.removeAll(removed)
                return true
            }
        }
        return false
    }

    override fun deleteProcessor(name: String): Boolean {
        val removed = processors.remove(name)
        return removed != null
    }

    override fun getAllProcessorConfig(): List<ProcessorConfig> {
        return processors.values.toList()
    }

    override fun getAllComponentConfig(): Map<String, List<ComponentConfig>> {
        return components
    }

    override fun getInstanceProps(name: String): Properties {
        return instances[name] ?: throw NullPointerException("No instance found for name $name")
    }
}