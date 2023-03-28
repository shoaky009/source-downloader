package xyz.shoaky.sourcedownloader.qbittorrent

import com.fasterxml.jackson.annotation.JsonProperty

//TODO 补充字段
data class TorrentInfo(
    @JsonProperty("amount_left")
    val amountLeft: Long,
    val hash: String,
    val size: Long,
    val progress: Float
)