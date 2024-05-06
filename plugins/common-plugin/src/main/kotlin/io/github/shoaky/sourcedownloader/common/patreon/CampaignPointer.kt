package io.github.shoaky.sourcedownloader.common.patreon

import io.github.shoaky.sourcedownloader.sdk.ItemPointer
import java.time.YearMonth

data class CampaignPointer(
    val campaignId: Long,
    /**
     * 已完成的yearMonth
     */
    val lastYearMonth: YearMonth? = null,
    /**
     * 该年月下最后一个，该字段用于恢复之后是否使用下一个月来请求
     */
    val lastOfMonth: Boolean = false,
    val lastPostId: Long = -1L,
) : ItemPointer