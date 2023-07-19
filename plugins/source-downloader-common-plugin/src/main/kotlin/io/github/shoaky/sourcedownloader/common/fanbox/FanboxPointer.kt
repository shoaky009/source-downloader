package io.github.shoaky.sourcedownloader.common.fanbox

import io.github.shoaky.sourcedownloader.sdk.SourceItemPointer

/**
 * Key is creatorId
 */
data class FanboxPointer(
    val creatorPointers: MutableMap<String, CreatorPointer> = mutableMapOf(),
    val supportingCount: Int
) : SourceItemPointer