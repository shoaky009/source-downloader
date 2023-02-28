package xyz.shoaky.sourcedownloader.sdk

import java.net.URL
import java.nio.file.Path

data class DownloadTask(
    val downloadUrl: URL,
    val downloadPath: Path?,
    val category: String? = null,
    val torrentHash: String? = null,
    val filename: String?,
) {

    companion object {

        fun createTorrentTask(torrentUrl: URL, torrentHash: String,
                              downloadPath: Path? = null, category: String? = null): DownloadTask {
            return create(torrentUrl, category, torrentHash, downloadPath)
        }

        fun create(
            downloadUrl: URL,
            category: String? = null,
            torrentHash: String? = null,
            downloadPath: Path? = null,
            filename: String? = null,
        ): DownloadTask {
            return DownloadTask(downloadUrl, downloadPath, category, torrentHash, filename)
        }
    }
}