package io.github.shoaky.sourcedownloader.external.bilibili

class BilibiliResponse<T>(
    val code: Int,
    val message: String? = null,
    val ttl: Int? = null,
    val data: T
)