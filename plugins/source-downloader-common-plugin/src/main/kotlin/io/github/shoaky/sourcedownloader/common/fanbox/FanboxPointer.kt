package io.github.shoaky.sourcedownloader.common.fanbox

import io.github.shoaky.sourcedownloader.sdk.ItemPointer
import io.github.shoaky.sourcedownloader.sdk.SourcePointer

/**
 * Key is creatorId
 */
data class FanboxPointer(
    val creatorPointers: MutableMap<String, CreatorPointer> = mutableMapOf(),
    val supportingCount: Int
) : SourcePointer {

    override fun update(itemPointer: ItemPointer) {
        if (itemPointer is CreatorPointer) {
            creatorPointers[itemPointer.creatorId] = itemPointer
        }
    }

    fun getOrDefault(creatorId: String): CreatorPointer {
        return creatorPointers[creatorId] ?: CreatorPointer(creatorId)
    }

}