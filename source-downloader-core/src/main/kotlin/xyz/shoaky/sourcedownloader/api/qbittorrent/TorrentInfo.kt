package xyz.shoaky.sourcedownloader.api.qbittorrent

import com.fasterxml.jackson.annotation.JsonProperty

//TODO 补充字段
data class TorrentInfo(
    @JsonProperty("amount_left")
    val amountLeft: Int,
    val hash: String,
    val size: Int,
    val progress: Float
)