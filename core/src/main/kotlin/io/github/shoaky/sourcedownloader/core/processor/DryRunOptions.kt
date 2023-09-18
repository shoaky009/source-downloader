package io.github.shoaky.sourcedownloader.core.processor

data class DryRunOptions(
    val pointer: Map<String, Any>? = null,
    val filterProcessed: Boolean = true
)