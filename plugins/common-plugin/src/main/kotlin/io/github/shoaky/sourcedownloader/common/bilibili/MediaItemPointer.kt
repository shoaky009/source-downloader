package io.github.shoaky.sourcedownloader.common.bilibili

import io.github.shoaky.sourcedownloader.sdk.ItemPointer

data class MediaItemPointer(
    val favoriteId: Long,
    val time: Long,
    val touchButton: Boolean = false
) : ItemPointer