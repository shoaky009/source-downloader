package xyz.shoaky.sourcedownloader.external.qbittorrent

import com.fasterxml.jackson.annotation.JsonProperty

data class TorrentInfo(
    @JsonProperty("amount_left")
    val amountLeft: Long,
    val hash: String,
    val size: Long,
    val progress: Float
)