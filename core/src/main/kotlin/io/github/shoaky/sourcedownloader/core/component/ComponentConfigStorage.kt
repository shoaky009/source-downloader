package io.github.shoaky.sourcedownloader.core.component

import io.github.shoaky.sourcedownloader.sdk.component.ComponentException
import io.github.shoaky.sourcedownloader.sdk.component.ComponentTopType

interface ComponentConfigStorage {

    /**
     * Key is [ComponentTopType], value is component config list
     */
    fun getAllComponentConfig(): Map<String, List<ComponentConfig>>

    fun getComponentConfig(type: ComponentTopType, typeName: String, name: String): ComponentConfig {
        return findComponentConfig(type, typeName, name)
            ?: throw ComponentException.missing("No component config found for $type:$typeName:$name")
    }

    fun findComponentConfig(type: ComponentTopType, typeName: String, name: String): ComponentConfig? {
        val names = type.names
        return getAllComponentConfig()
            .filter { names.contains(it.key) }
            .flatMap { it.value }
            .firstOrNull { it.type == typeName && it.name == name }
    }
}