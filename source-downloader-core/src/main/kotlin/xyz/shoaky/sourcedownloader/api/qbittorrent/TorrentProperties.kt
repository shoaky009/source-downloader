package xyz.shoaky.sourcedownloader.api.qbittorrent

data class TorrentProperties(
    val name: String,
    val size: Int,
    val progress: Float
)