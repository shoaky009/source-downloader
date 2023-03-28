package xyz.shoaky.sourcedownloader.sdk.component

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import xyz.shoaky.sourcedownloader.sdk.util.Jackson
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

class ComponentProps
private constructor(val properties: MutableMap<String, Any>) {

    inline fun <reified T> parse(): T {
        return Jackson.convert(properties, jacksonTypeRef())
    }

    inline fun <reified T> get(key: String): T {
        val any = properties[key] ?: throw ComponentException("属性不存在: $key")
        try {
            return Jackson.convert(any, jacksonTypeRef())
        } catch (e: Exception) {
            throw RuntimeException("组件属性解析异常: $key", e)
        }
    }

    inline fun <reified T> getOrDefault(key: String, default: T): T {
        val any = properties[key] ?: return default
        try {
            return Jackson.convert(any, jacksonTypeRef())
        } catch (e: Exception) {
            throw RuntimeException("组件属性解析异常: $key", e)
        }
    }

    companion object {

        fun fromMap(map: Map<String, Any>): ComponentProps {
            val mutableMapOf = mutableMapOf<String, Any>()
            mutableMapOf.putAll(map)
            for (entry in map) {
                val value = entry.value
                if (value is Map<*, *> && value["0"] != null) {
                    // yaml中的数组会被解析成map
                    mutableMapOf[entry.key] = value.values
                }
            }

            return ComponentProps(mutableMapOf)
        }

        fun fromJson(json: String): ComponentProps {
            val typeReference = jacksonTypeRef<MutableMap<String, Any>>()
            return ComponentProps(Jackson.fromJson(json, typeReference))
        }

    }
}

interface SdComponentSupplier<R : SdComponent> {

    fun apply(props: ComponentProps): R
    fun supplyTypes(): List<ComponentType>

    fun rules(): List<ComponentRule> = emptyList()

    /**
     * 后面反射用
     */
    fun getComponentClass(): Class<R>
}

data class ComponentRule internal constructor(
    val isAllow: Boolean,
    val type: Components,
    val value: KClass<out SdComponent>) {

    fun isSameType(klass: KClass<out SdComponent>): Boolean {
        return type.klass.isSuperclassOf(klass)
    }

    fun isSameType(component: SdComponent): Boolean {
        return type.klass.isSuperclassOf(component::class)
    }

    fun verify(component: SdComponent) {
        if (isSameType(component).not()) {
            return
        }

        val componentClasses = component::class.componentSuperClasses()
        if (isAllow) {
            if (componentClasses.contains(value).not()) {
                val classes = componentClasses.map { it.simpleName }.joinToString(",")
                throw ComponentException("组件类型不匹配, 期望:${value.simpleName}, 实际:$classes")
            }
        } else {
            if (componentClasses.contains(value)) {
                throw ComponentException("组件类型不匹配, ${value.simpleName}不允许和${component::class.simpleName}组合")
            }
        }
    }

    companion object {

        fun allow(type: Components, value: KClass<out SdComponent>) = ComponentRule(true, type, value)
        fun notAllow(type: Components, value: KClass<out SdComponent>) = ComponentRule(false, type, value)

        fun allowSource(value: KClass<out SdComponent>) = ComponentRule(true, Components.SOURCE, value)
        fun notAllowSource(value: KClass<out SdComponent>) = ComponentRule(false, Components.SOURCE, value)
        fun allowDownloader(value: KClass<out SdComponent>) = ComponentRule(true, Components.DOWNLOADER, value)
        fun notAllowDownloader(value: KClass<out SdComponent>) = ComponentRule(false, Components.DOWNLOADER, value)
        fun allowProvider(value: KClass<out SdComponent>) = ComponentRule(true, Components.VARIABLE_PROVIDER, value)
        fun notAllowProvider(value: KClass<out SdComponent>) =
            ComponentRule(false, Components.VARIABLE_PROVIDER, value)

        fun allowTrigger(value: KClass<out SdComponent>) = ComponentRule(true, Components.TRIGGER, value)
        fun notAllowTrigger(value: KClass<out SdComponent>) = ComponentRule(false, Components.TRIGGER, value)
        fun allowMover(value: KClass<out SdComponent>) = ComponentRule(true, Components.FILE_MOVER, value)
        fun notAllowMover(value: KClass<out SdComponent>) = ComponentRule(false, Components.FILE_MOVER, value)
        fun allowRunAfterCompletion(value: KClass<out SdComponent>) =
            ComponentRule(true, Components.RUN_AFTER_COMPLETION, value)

        fun notAllowRunAfterCompletion(value: KClass<out SdComponent>) =
            ComponentRule(false, Components.RUN_AFTER_COMPLETION, value)

        fun allowSourceFilter(value: KClass<out SdComponent>) = ComponentRule(true, Components.SOURCE_FILTER, value)
        fun notAllowSourceFilter(value: KClass<out SdComponent>) = ComponentRule(false, Components.SOURCE_FILTER, value)
    }
}

