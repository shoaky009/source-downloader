package io.github.shoaky.sourcedownloader.external.qbittorrent

data class TorrentProperties(
    val name: String,
    val size: Int,
    val progress: Float
)