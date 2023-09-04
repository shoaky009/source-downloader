package io.github.shoaky.sourcedownloader.common.patreon

import io.github.shoaky.sourcedownloader.sdk.ItemPointer

data class CampaignPointer(
    val campaignId: Long,
    val cursor: String? = null,
    val lastPostId: Long = -1L,
) : ItemPointer