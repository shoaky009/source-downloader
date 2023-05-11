package xyz.shoaky.sourcedownloader.sdk

import xyz.shoaky.sourcedownloader.sdk.component.ComponentException
import xyz.shoaky.sourcedownloader.sdk.component.Components
import xyz.shoaky.sourcedownloader.sdk.component.SdComponent
import xyz.shoaky.sourcedownloader.sdk.component.componentSuperClasses
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

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
        fun allowFileResolver(value: KClass<out SdComponent>) = ComponentRule(true, Components.ITEM_FILE_RESOLVER, value)

        @JvmStatic
        fun notAllowFileResolver(value: KClass<out SdComponent>) = ComponentRule(false, Components.ITEM_FILE_RESOLVER, value)

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