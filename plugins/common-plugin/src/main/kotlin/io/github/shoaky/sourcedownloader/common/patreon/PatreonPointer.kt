package io.github.shoaky.sourcedownloader.common.patreon

import io.github.shoaky.sourcedownloader.sdk.ItemPointer
import io.github.shoaky.sourcedownloader.sdk.SourcePointer

data class PatreonPointer(
    val campaignPointers: MutableMap<Long, CampaignPointer> = mutableMapOf()
) : SourcePointer {

    override fun update(itemPointer: ItemPointer) {
        if (itemPointer !is CampaignPointer) {
            return
        }
        campaignPointers[itemPointer.campaignId] = itemPointer
    }
}