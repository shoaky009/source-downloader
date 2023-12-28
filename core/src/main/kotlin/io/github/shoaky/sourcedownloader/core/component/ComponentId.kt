package io.github.shoaky.sourcedownloader.core.component

import com.fasterxml.jackson.annotation.JsonValue
import io.github.shoaky.sourcedownloader.sdk.component.ComponentException
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.SdComponent
import kotlin.reflect.KClass

data class ComponentId(
    @get:JsonValue
    val id: String,
) {

    fun <T : SdComponent> getInstanceName(klass: KClass<T>): String {
        return getComponentType(klass).instanceName(name())
    }

    fun <T : SdComponent> getComponentType(klass: KClass<T>): ComponentType {
        return ComponentType(typeName(), klass)
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
}