package xyz.shoaky.sourcedownloader.sdk

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import xyz.shoaky.sourcedownloader.sdk.component.*
import xyz.shoaky.sourcedownloader.sdk.util.Jackson
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

class Properties
private constructor(
    val rawValues: MutableMap<String, Any>
) {

    inline fun <reified T> parse(): T {
        return Jackson.convert(rawValues, jacksonTypeRef())
    }

    inline fun <reified T> get(key: String): T {
        val any = rawValues[key] ?: throw ComponentException.props("属性${key}不存在")
        try {
            return Jackson.convert(any, jacksonTypeRef())
        } catch (e: Exception) {
            throw RuntimeException("属性${key}解析异常: value:$any", e)
        }
    }

    inline fun <reified T> getNotRequired(key: String): T? {
        val any = rawValues[key] ?: return null
        try {
            return Jackson.convert(any, jacksonTypeRef())
        } catch (e: Exception) {
            throw RuntimeException("属性${key}解析异常: value:$any", e)
        }
    }

    fun getRaw(key: String): Any {
        return rawValues[key] ?: throw ComponentException.props("属性${key}不存在")
    }

    inline fun <reified T> getOrDefault(key: String, default: T): T {
        val any = rawValues[key] ?: return default
        try {
            return Jackson.convert(any, jacksonTypeRef())
        } catch (e: Exception) {
            throw RuntimeException("属性解析异常: $key", e)
        }
    }

    companion object {

        private val emptyProps = Properties(mutableMapOf())

        @JvmStatic
        fun fromMap(map: Map<String, Any>): Properties {
            val mutableMapOf = mutableMapOf<String, Any>()
            mutableMapOf.putAll(map)
            for (entry in map) {
                val value = entry.value
                if (value is Map<*, *> && value["0"] != null) {
                    // yaml中的数组会被解析成map
                    mutableMapOf[entry.key] = value.values
                }
            }

            return Properties(mutableMapOf)
        }

        @JvmStatic
        fun fromJson(json: String): Properties {
            val typeReference = jacksonTypeRef<MutableMap<String, Any>>()
            return Properties(Jackson.fromJson(json, typeReference))
        }

        @JvmStatic
        fun empty(): Properties {
            return emptyProps
        }

    }
}

interface SdComponentSupplier<R : SdComponent> {

    fun apply(props: Properties): R
    fun supplyTypes(): List<ComponentType>

    fun rules(): List<ComponentRule> = emptyList()
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
                throw ComponentException.compatibility("组件类型不匹配, 期望:${value.simpleName}, 实际:$classes")
            }
        } else {
            if (componentClasses.contains(value)) {
                throw ComponentException.compatibility("组件类型不匹配, ${value.simpleName}不允许和${component::class.simpleName}组合")
            }
        }
    }

    companion object {

        @JvmStatic
        fun allow(type: Components, value: KClass<out SdComponent>) = ComponentRule(true, type, value)
        @JvmStatic
        fun notAllow(type: Components, value: KClass<out SdComponent>) = ComponentRule(false, type, value)

        @JvmStatic
        fun allowSource(value: KClass<out SdComponent>) = ComponentRule(true, Components.SOURCE, value)
        @JvmStatic
        fun notAllowSource(value: KClass<out SdComponent>) = ComponentRule(false, Components.SOURCE, value)
        @JvmStatic
        fun allowDownloader(value: KClass<out SdComponent>) = ComponentRule(true, Components.DOWNLOADER, value)
        @JvmStatic
        fun notAllowDownloader(value: KClass<out SdComponent>) = ComponentRule(false, Components.DOWNLOADER, value)
        @JvmStatic
        fun allowProvider(value: KClass<out SdComponent>) = ComponentRule(true, Components.VARIABLE_PROVIDER, value)
        @JvmStatic
        fun notAllowProvider(value: KClass<out SdComponent>) =
            ComponentRule(false, Components.VARIABLE_PROVIDER, value)
        @JvmStatic
        fun allowTrigger(value: KClass<out SdComponent>) = ComponentRule(true, Components.TRIGGER, value)
        @JvmStatic
        fun notAllowTrigger(value: KClass<out SdComponent>) = ComponentRule(false, Components.TRIGGER, value)
        @JvmStatic
        fun allowMover(value: KClass<out SdComponent>) = ComponentRule(true, Components.FILE_MOVER, value)
        @JvmStatic
        fun notAllowMover(value: KClass<out SdComponent>) = ComponentRule(false, Components.FILE_MOVER, value)
        @JvmStatic
        fun allowRunAfterCompletion(value: KClass<out SdComponent>) =
            ComponentRule(true, Components.RUN_AFTER_COMPLETION, value)

        @JvmStatic
        fun notAllowRunAfterCompletion(value: KClass<out SdComponent>) =
            ComponentRule(false, Components.RUN_AFTER_COMPLETION, value)

        @JvmStatic
        fun allowSourceFilter(value: KClass<out SdComponent>) = ComponentRule(true, Components.SOURCE_ITEM_FILTER, value)
        @JvmStatic
        fun notAllowSourceFilter(value: KClass<out SdComponent>) = ComponentRule(false, Components.SOURCE_ITEM_FILTER, value)
    }
}

