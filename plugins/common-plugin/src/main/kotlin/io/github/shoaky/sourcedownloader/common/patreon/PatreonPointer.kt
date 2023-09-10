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

        // 当获取到最新的数据时, patreon的api不会返回游标需要保留上一次最新的游标方便下次请求
            campaignPointers[itemPointer.campaignId] = itemPointer
        // if (itemPointer.cursor != null) {
        // } else {
        //     val cursor = campaignPointers[itemPointer.campaignId]?.cursor
        //     campaignPointers[itemPointer.campaignId] = itemPointer.copy(cursor = cursor)
        // }
    }
}