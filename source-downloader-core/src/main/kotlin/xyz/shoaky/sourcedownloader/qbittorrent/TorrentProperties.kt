package xyz.shoaky.sourcedownloader.qbittorrent

data class TorrentProperties(
    val name: String,
    val size: Int,
    val progress: Float
)