package io.github.shoaky.sourcedownloader.sdk.component

import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

@Suppress("UNUSED")
data class ComponentRule(
    val isAllow: Boolean,
    val type: ComponentRootType,
    val value: KClass<out SdComponent>
) {

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
                throw ComponentException.compatibility("组件${type.primaryName}不匹配, 期望:${value.simpleName}, 实际:${component::class.simpleName}")
            }
        } else {
            if (componentClasses.contains(value)) {
                throw ComponentException.compatibility("组件${type.primaryName}不匹配, ${value.simpleName}不允许和${component::class.simpleName}组合")
            }
        }
    }

    companion object {

        @JvmStatic
        fun allow(type: ComponentRootType, value: KClass<out SdComponent>) = ComponentRule(true, type, value)

        @JvmStatic
        fun notAllow(type: ComponentRootType, value: KClass<out SdComponent>) = ComponentRule(false, type, value)

        @JvmStatic
        fun allowSource(value: KClass<out SdComponent>) = ComponentRule(true, ComponentRootType.SOURCE, value)

        @JvmStatic
        fun notAllowSource(value: KClass<out SdComponent>) = ComponentRule(false, ComponentRootType.SOURCE, value)

        @JvmStatic
        fun allowDownloader(value: KClass<out SdComponent>) = ComponentRule(true, ComponentRootType.DOWNLOADER, value)

        @JvmStatic
        fun notAllowDownloader(value: KClass<out SdComponent>) =
            ComponentRule(false, ComponentRootType.DOWNLOADER, value)

        @JvmStatic
        fun allowProvider(value: KClass<out SdComponent>) =
            ComponentRule(true, ComponentRootType.VARIABLE_PROVIDER, value)

        @JvmStatic
        fun allowFileResolver(value: KClass<out SdComponent>) =
            ComponentRule(true, ComponentRootType.ITEM_FILE_RESOLVER, value)

        @JvmStatic
        fun notAllowFileResolver(value: KClass<out SdComponent>) =
            ComponentRule(false, ComponentRootType.ITEM_FILE_RESOLVER, value)

        @JvmStatic
        fun notAllowProvider(value: KClass<out SdComponent>) =
            ComponentRule(false, ComponentRootType.VARIABLE_PROVIDER, value)

        @JvmStatic
        fun allowTrigger(value: KClass<out SdComponent>) = ComponentRule(true, ComponentRootType.TRIGGER, value)

        @JvmStatic
        fun notAllowTrigger(value: KClass<out SdComponent>) = ComponentRule(false, ComponentRootType.TRIGGER, value)

        @JvmStatic
        fun allowMover(value: KClass<out SdComponent>) = ComponentRule(true, ComponentRootType.FILE_MOVER, value)

        @JvmStatic
        fun notAllowMover(value: KClass<out SdComponent>) = ComponentRule(false, ComponentRootType.FILE_MOVER, value)

        @JvmStatic
        fun allowRunAfterCompletion(value: KClass<out SdComponent>) =
            ComponentRule(true, ComponentRootType.PROCESS_LISTENER, value)

        @JvmStatic
        fun notAllowRunAfterCompletion(value: KClass<out SdComponent>) =
            ComponentRule(false, ComponentRootType.PROCESS_LISTENER, value)

        @JvmStatic
        fun allowSourceFilter(value: KClass<out SdComponent>) =
            ComponentRule(true, ComponentRootType.SOURCE_ITEM_FILTER, value)

        @JvmStatic
        fun notAllowSourceFilter(value: KClass<out SdComponent>) =
            ComponentRule(false, ComponentRootType.SOURCE_ITEM_FILTER, value)
    }
}