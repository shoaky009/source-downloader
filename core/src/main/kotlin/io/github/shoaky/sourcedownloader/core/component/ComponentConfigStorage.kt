package io.github.shoaky.sourcedownloader.core.component

import io.github.shoaky.sourcedownloader.sdk.component.ComponentTopType
import io.github.shoaky.sourcedownloader.throwComponentException

interface ComponentConfigStorage {

    /**
     * Key is [ComponentTopType], value is component config list
     */
    fun getAllComponentConfig(): Map<String, List<ComponentConfig>>

    fun getComponentConfig(type: ComponentTopType, typeName: String, name: String): ComponentConfig {
        return findComponentConfig(type, typeName, name)
            ?: throwComponentException(
                "No component config found for $type:$typeName:$name",
                ComponentFailureType.DEFINITION_NOT_FOUND
            )
    }

    fun findComponentConfig(type: ComponentTopType, typeName: String, name: String): ComponentConfig? {
        val names = type.alias
        return getAllComponentConfig()
            .filter { names.contains(it.key) }
            .flatMap { it.value }
            .firstOrNull { it.type == typeName && it.name == name }
    }
}