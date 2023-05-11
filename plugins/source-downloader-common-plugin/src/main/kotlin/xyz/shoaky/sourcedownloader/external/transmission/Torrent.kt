package xyz.shoaky.sourcedownloader.external.transmission

import java.nio.file.Path

data class Torrent(
    val id: Long,
    val name: String,
    val hashString: String,
    val isFinished: Boolean,
    val status: Int,
    val files: List<TorrentFile>,
    val fileStats: List<FileStats>,
    val percentComplete: Double,
)

data class TorrentFile(
    val name: Path,
    val length: Long
)

data class FileStats(
    val priority: Int,
    val wanted: Boolean
)