package io.github.shoaky.sourcedownloader.common.pixiv

import io.github.shoaky.sourcedownloader.sdk.ItemPointer
import io.github.shoaky.sourcedownloader.sdk.SourcePointer

data class PixivPointer(
    // key is userId value is the latest illust id that already fetched
    val lastIllustrationRecord: MutableMap<Long, Long> = mutableMapOf(),
    var topBookmarkId: String = "0",
    var currentBookmarkId: String? = null,
    var touchBottom: Boolean = false,
) : SourcePointer {

    override fun update(itemPointer: ItemPointer) {
        when (itemPointer) {
            is IllustrationPointer -> {
                lastIllustrationRecord[itemPointer.userId] = itemPointer.illustrationId
            }

            is BookmarkPointer -> {
                if (topBookmarkId < itemPointer.bookmarkId) {
                    topBookmarkId = itemPointer.bookmarkId
                }
                currentBookmarkId = itemPointer.bookmarkId
            }
        }
    }
}