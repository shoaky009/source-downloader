package io.github.shoaky.sourcedownloader.common.fanbox

import io.github.shoaky.sourcedownloader.sdk.ItemPointer
import io.github.shoaky.sourcedownloader.sdk.SourcePointer

/**
 * Key is creatorId
 */
data class FanboxPointer(
    val creatorPointers: MutableMap<String, CreatorPointer> = mutableMapOf()
) : SourcePointer {

    override fun update(itemPointer: ItemPointer) {
        if (itemPointer is CreatorPointer) {
            creatorPointers[itemPointer.creatorId] = itemPointer
        }
    }

}