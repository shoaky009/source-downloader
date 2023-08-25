package io.github.shoaky.sourcedownloader.external.qbittorrent

import java.nio.file.Path

data class TorrentFile(
    val index: Int,
    val name: Path,
    val progress: Float,
    val size: Int,
    /**
     * 0	Do not download
     * 1	Normal priority
     * 6	High priority
     * 7	Maximal priority
     */
    val priority: Int
)