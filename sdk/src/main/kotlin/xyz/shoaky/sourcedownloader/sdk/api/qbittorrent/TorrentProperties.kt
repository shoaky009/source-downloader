package xyz.shoaky.sourcedownloader.sdk.api.qbittorrent

data class TorrentProperties(
    val name: String,
    val size: Int,
    val progress: Float
)