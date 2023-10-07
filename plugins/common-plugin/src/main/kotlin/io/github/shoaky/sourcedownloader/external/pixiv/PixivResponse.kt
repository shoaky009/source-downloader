package io.github.shoaky.sourcedownloader.external.pixiv

data class PixivResponse<T>(
    val body: T,
    val error: Boolean = false,
    val message: String? = null,
)