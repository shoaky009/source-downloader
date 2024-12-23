package io.github.shoaky.sourcedownloader.sdk.component

data class ComponentMetadata(
    val description: String? = null,
    val propertySchema: JsonSchema,
    val uiSchema: Map<String, Any> = emptyMap()
)