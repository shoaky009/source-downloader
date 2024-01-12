package io.github.shoaky.sourcedownloader.external.qbittorrent

import com.fasterxml.jackson.annotation.JsonProperty
import java.nio.file.Path

data class TorrentInfo(
    @JsonProperty("amount_left")
    val amountLeft: Long,
    val hash: String,
    val size: Long,
    val progress: Float,
    @JsonProperty("content_path")
    val contentPath: Path,
)