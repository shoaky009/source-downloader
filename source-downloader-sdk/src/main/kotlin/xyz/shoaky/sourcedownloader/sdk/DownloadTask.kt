package xyz.shoaky.sourcedownloader.sdk

import java.net.URL
import java.nio.file.Path

data class DownloadTask(
    val sourceItem: SourceItem,
    val downloadPath: Path?,
    val category: String? = null,
    val torrentHash: String? = null,
) {

    fun downloadURL(): URL {
        return sourceItem.downloadUrl
    }

    companion object {

        fun createTorrentTask(sourceItem: SourceItem, torrentHash: String,
                              downloadPath: Path? = null, category: String? = null): DownloadTask {
            return create(sourceItem, category, torrentHash, downloadPath)
        }

        fun create(
            sourceItem: SourceItem,
            category: String? = null,
            torrentHash: String? = null,
            downloadPath: Path? = null,
        ): DownloadTask {
            return DownloadTask(sourceItem, downloadPath, category, torrentHash)
        }
    }
}