package io.github.shoaky.sourcedownloader.external.pixiv

data class GetUserAll(
    // key is id
    val illusts: Map<Long, Any?> = emptyMap(),
)