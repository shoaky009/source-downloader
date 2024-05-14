package io.github.shoaky.sourcedownloader.core.component

import com.fasterxml.jackson.annotation.JsonValue
import io.github.shoaky.sourcedownloader.sdk.component.ComponentException
import io.github.shoaky.sourcedownloader.sdk.component.ComponentTopType
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

data class ComponentId(
    @get:JsonValue
    val id: String,
) {

    fun getInstanceName(topType: ComponentTopType): String {
        return getComponentType(topType).instanceName(name())
    }

    fun getComponentType(topType: ComponentTopType): ComponentType {
        return ComponentType(topType, typeName())
    }

    fun name(): String {
        return id.split(":").lastOrNull() ?: throw ComponentException.props("组件ID配置错误:${id}")
    }

    fun typeName(): String {
        return id.split(":").firstOrNull() ?: throw ComponentException.props("组件ID配置错误:${id}")
    }

    override fun toString(): String {
        return id
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ComponentId) return false

        if (id == other.id) return true

        val split = id.split(":")
        val otherSplit = other.id.split(":")
        return split.first() == otherSplit.first() && split.last() == otherSplit.last()
    }
}