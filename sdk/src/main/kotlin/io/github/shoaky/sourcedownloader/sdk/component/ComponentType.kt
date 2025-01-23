package io.github.shoaky.sourcedownloader.sdk.component

import com.google.common.base.CaseFormat
import io.github.shoaky.sourcedownloader.sdk.component.ComponentTopType.*

data class ComponentType(
    val type: ComponentTopType,
    val typeName: String,
) {

    init {
        if (topTypeClasses.contains(type.klass).not()) {
            throw IllegalArgumentException("topTypeClass must be one of $topTypeClasses")
        }
    }

    @Suppress("UNUSED")
    companion object {

        @JvmStatic
        fun downloader(typeName: String) = ComponentType(DOWNLOADER, typeName)

        @JvmStatic
        fun source(typeName: String) = ComponentType(SOURCE, typeName)

        @JvmStatic
        fun fileMover(typeName: String) = ComponentType(FILE_MOVER, typeName)

        @JvmStatic
        fun variableProvider(typeName: String) = ComponentType(VARIABLE_PROVIDER, typeName)

        @JvmStatic
        fun fileResolver(typeName: String) = ComponentType(ITEM_FILE_RESOLVER, typeName)

        @JvmStatic
        fun itemFilter(typeName: String) = ComponentType(SOURCE_ITEM_FILTER, typeName)

        @JvmStatic
        fun itemContentFilter(typeName: String) = ComponentType(ITEM_CONTENT_FILTER, typeName)

        @JvmStatic
        fun trigger(typeName: String) = ComponentType(TRIGGER, typeName)

        @JvmStatic
        fun listener(typeName: String) = ComponentType(PROCESS_LISTENER, typeName)
        
        @JvmStatic
        fun sourceFileFilter(typeName: String) = ComponentType(SOURCE_FILE_FILTER, typeName)

        @JvmStatic
        fun fileContentFilter(typeName: String) = ComponentType(FILE_CONTENT_FILTER, typeName)

        @JvmStatic
        fun fileTagger(typeName: String): ComponentType = ComponentType(TAGGER, typeName)

        @JvmStatic
        fun fileReplacementDecider(typeName: String): ComponentType = ComponentType(FILE_REPLACEMENT_DECIDER, typeName)

        @JvmStatic
        fun itemExistsDetector(typeName: String): ComponentType = ComponentType(FILE_EXISTS_DETECTOR, typeName)

        @JvmStatic
        fun variableReplacer(typeName: String): ComponentType = ComponentType(VARIABLE_REPLACER, typeName)

        @JvmStatic
        fun manualSource(typeName: String): ComponentType = ComponentType(MANUAL_SOURCE, typeName)

        @JvmStatic
        fun types(): List<String> {
            return componentTypes.keys.toList()
        }

        @JvmStatic
        fun of(topType: ComponentTopType, typeName: String): ComponentType {
            return ComponentType(topType, typeName)
        }

        val topTypeClasses = SdComponent::class.sealedSubclasses
        private val componentTypes = topTypeClasses
            .associateBy {
                val simpleName = it.simpleName!!
                CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, simpleName)
            }
    }

    fun fullName() = "${type.primaryName}:$typeName"

    fun instanceName(name: String) = "${fullName()}:${name}"

}