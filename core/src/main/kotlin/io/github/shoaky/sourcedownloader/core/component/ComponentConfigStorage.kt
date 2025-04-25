package io.github.shoaky.sourcedownloader.core.component

import io.github.shoaky.sourcedownloader.sdk.component.ComponentRootType
import io.github.shoaky.sourcedownloader.throwComponentException

interface ComponentConfigStorage {

    /**
     * Key is [ComponentRootType], value is component config list
     */
    fun getAllComponentConfig(): Map<String, List<ComponentConfig>>

    fun getComponentConfig(type: ComponentRootType, typeName: String, name: String): ComponentConfig {
        return findComponentConfig(type, typeName, name)
            ?: throwComponentException(
                "No component config found for $type:$typeName:$name",
                ComponentFailureType.DEFINITION_NOT_FOUND
            )
    }

    fun findComponentConfig(type: ComponentRootType, typeName: String, name: String): ComponentConfig? {
        val names = type.alias
        return getAllComponentConfig()
            .filter { names.contains(it.key) }
            .flatMap { it.value }
            .firstOrNull { it.type == typeName && it.name == name }
    }
}