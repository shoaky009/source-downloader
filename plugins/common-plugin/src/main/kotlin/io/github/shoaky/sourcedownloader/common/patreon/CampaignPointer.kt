package io.github.shoaky.sourcedownloader.common.patreon

import io.github.shoaky.sourcedownloader.sdk.ItemPointer
import java.time.YearMonth

data class CampaignPointer(
    val campaignId: Long,
    /**
     * 已完成的yearMonth
     */
    val lastYearMonth: YearMonth? = null,
    val lastPostId: Long = -1L,
) : ItemPointer