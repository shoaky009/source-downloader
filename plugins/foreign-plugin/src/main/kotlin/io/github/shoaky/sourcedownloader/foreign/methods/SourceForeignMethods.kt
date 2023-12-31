package io.github.shoaky.sourcedownloader.foreign.methods

data class SourceForeignMethods(
    val fetch: String = "/source/fetch",
    val next: String = "/source/next",
    val hasNext: String = "/source/has_next",
    val pointerUpdate: String = "/source/pointer_update",
)