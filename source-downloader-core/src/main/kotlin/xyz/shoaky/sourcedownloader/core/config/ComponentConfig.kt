package xyz.shoaky.sourcedownloader.core.config

data class ComponentConfig(
    val name: String,
    val type: String,
    val props: Map<String, Any> = emptyMap()
)