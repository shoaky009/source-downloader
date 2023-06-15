package io.github.shoaky.sourcedownloader.core.component

data class ComponentConfig(
    val name: String,
    val type: String,
    val props: Map<String, Any> = emptyMap()
)