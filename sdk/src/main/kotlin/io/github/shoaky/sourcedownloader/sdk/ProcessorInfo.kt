package io.github.shoaky.sourcedownloader.sdk

import java.nio.file.Path

data class ProcessorInfo(
    val name: String,
    val downloadPath: Path,
    val sourceSavePath: Path,
    val tags: List<String> = emptyList(),
    val category: String? = null,
    // val components: Map<String, Any>
)