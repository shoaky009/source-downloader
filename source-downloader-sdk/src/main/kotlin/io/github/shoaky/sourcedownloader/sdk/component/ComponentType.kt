package io.github.shoaky.sourcedownloader.sdk.component

import com.google.common.base.CaseFormat
import kotlin.reflect.KClass

data class ComponentType(
    val typeName: String,
    // TODO 限制密封类
    val topTypeClass: KClass<out SdComponent>
) {

    constructor(typeName: String, type: ComponentTopType) : this(typeName, type.klass)

    fun topType(): ComponentTopType {
        return ComponentTopType.fromClass(topTypeClass).first()
    }


    @Suppress("UNUSED")
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
        fun fileResolver(type: String) = ComponentType(type, ItemFileResolver::class)

        @JvmStatic
        fun itemFilter(type: String) = ComponentType(type, SourceItemFilter::class)

        @JvmStatic
        fun trigger(type: String) = ComponentType(type, Trigger::class)

        @JvmStatic
        fun run(type: String) = ComponentType(type, RunAfterCompletion::class)

        @JvmStatic
        fun fileFilter(type: String) = ComponentType(type, FileContentFilter::class)

        @JvmStatic
        fun fileTagger(type: String): ComponentType {
            return ComponentType(type, FileTagger::class)
        }

        @JvmStatic
        fun fileReplacementDecider(type: String): ComponentType {
            return ComponentType(type, FileReplacementDecider::class)
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