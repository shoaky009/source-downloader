package io.github.shoaky.sourcedownloader.sdk.component

import com.google.common.base.CaseFormat
import kotlin.reflect.KClass

data class ComponentType(
    val typeName: String,
    val topTypeClass: KClass<out SdComponent>
) {

    init {
        if (topTypeClasses.contains(topTypeClass).not()) {
            throw IllegalArgumentException("topTypeClass must be one of $topTypeClasses")
        }
    }

    val topType: ComponentTopType
        get() = topType()

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
        fun contentFilter(type: String) = ComponentType(type, ItemContentFilter::class)

        @JvmStatic
        fun trigger(type: String) = ComponentType(type, Trigger::class)

        @JvmStatic
        fun run(type: String) = ComponentType(type, RunAfterCompletion::class)

        @JvmStatic
        fun fileFilter(type: String) = ComponentType(type, FileContentFilter::class)

        @JvmStatic
        fun fileTagger(type: String): ComponentType = ComponentType(type, FileTagger::class)

        @JvmStatic
        fun fileReplacementDecider(type: String): ComponentType = ComponentType(type, FileReplacementDecider::class)

        @JvmStatic
        fun itemExistsDetector(type: String): ComponentType = ComponentType(type, ItemExistsDetector::class)

        @JvmStatic
        fun manualSource(type: String): ComponentType = ComponentType(type, ManualSource::class)

        @JvmStatic
        fun types(): List<String> {
            return componentTypes.keys.toList()
        }

        @JvmStatic
        fun of(topType: ComponentTopType, typeName: String): ComponentType {
            return ComponentType(typeName, topType.klass)
        }

        val topTypeClasses = SdComponent::class.sealedSubclasses
        private val componentTypes = topTypeClasses
            .associateBy {
                val simpleName = it.simpleName!!
                CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, simpleName)
            }
    }

    fun fullName() = "${topTypeClass.simpleName}:$typeName"

    fun instanceName(name: String) = "${fullName()}:${name}"

}