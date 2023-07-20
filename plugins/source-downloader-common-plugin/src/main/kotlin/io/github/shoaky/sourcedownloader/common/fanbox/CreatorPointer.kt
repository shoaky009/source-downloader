package io.github.shoaky.sourcedownloader.common.fanbox

import io.github.shoaky.sourcedownloader.sdk.ItemPointer

data class CreatorPointer(
    val creatorId: String,
    val page: Int = -1,
    val itemIndex: Int = 0
) : ItemPointer