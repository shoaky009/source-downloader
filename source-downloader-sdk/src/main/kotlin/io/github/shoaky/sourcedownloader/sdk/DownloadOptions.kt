package io.github.shoaky.sourcedownloader.sdk

data class DownloadOptions(
    val category: String? = null,
    val tags: List<String> = emptyList(),
    val preferFileUri: Boolean = false
)