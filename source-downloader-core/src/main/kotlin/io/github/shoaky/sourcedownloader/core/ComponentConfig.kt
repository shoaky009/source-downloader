package io.github.shoaky.sourcedownloader.core

data class ComponentConfig(
    val name: String,
    val type: String,
    val props: Map<String, Any> = emptyMap()
)