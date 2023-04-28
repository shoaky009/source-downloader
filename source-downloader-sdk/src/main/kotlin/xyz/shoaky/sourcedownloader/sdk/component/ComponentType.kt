package xyz.shoaky.sourcedownloader.sdk.component

import com.google.common.base.CaseFormat
import kotlin.reflect.KClass

data class ComponentType(
    val typeName: String,
    // TODO 限制密封类
    val topTypeClass: KClass<out SdComponent>
) {

    constructor(type: String, name: String) : this(name, componentTypes[type]
        ?: throw ComponentException.unsupported("不支持的类型$type, 支持的类型有${componentTypes.keys}"))

    fun topType(): Components {
        return Components.fromClass(topTypeClass)
        // 不应该出现
            ?: throw ComponentException.unsupported("不支持的类型${topTypeClass.simpleName}")
    }


    companion object {
        @JvmStatic
        fun downloader(type: String) = ComponentType(type, Downloader::class)
        @JvmStatic
        fun source(type: String) = ComponentType(type, Source::class)
        @JvmStatic
        fun fileMover(type: String) = ComponentType(type, FileMover::class)
        @JvmStatic
        fun provider(type: String) = ComponentType(type, VariableProvider::class)
        @JvmStatic
        fun itemFilter(type: String) = ComponentType(type, SourceItemFilter::class)
        @JvmStatic
        fun trigger(type: String) = ComponentType(type, Trigger::class)
        @JvmStatic
        fun run(type: String) = ComponentType(type, RunAfterCompletion::class)
        @JvmStatic
        fun fileFilter(type: String) = ComponentType(type, SourceFileFilter::class)
        @JvmStatic
        fun fileTagger(type: String): ComponentType {
            return ComponentType(type, FileTagger::class)
        }

        private val componentTypes = SdComponent::class.sealedSubclasses
            .associateBy {
                val simpleName = it.simpleName!!
                CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, simpleName)
            }

        @JvmStatic
        fun typeOf(type: String): KClass<out SdComponent>? {
            return componentTypes[type]
        }

        @JvmStatic
        fun types(): List<String> {
            return componentTypes.keys.toList()
        }
    }

    fun fullName() = "${topTypeClass.simpleName}:$typeName"

    fun instanceName(name: String) = "${fullName()}:${name}"

}