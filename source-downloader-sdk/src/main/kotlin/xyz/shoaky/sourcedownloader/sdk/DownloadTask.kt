package xyz.shoaky.sourcedownloader.sdk

import java.net.URL
import java.nio.file.Path

data class DownloadTask(
    val sourceItem: SourceItem,
    val downloadPath: Path?,
    val category: String? = null,
) {

    fun downloadURL(): URL {
        return sourceItem.downloadUrl
    }

    companion object {

        fun create(
            sourceItem: SourceItem,
            category: String? = null,
            downloadPath: Path? = null,
        ): DownloadTask {
            return DownloadTask(sourceItem, downloadPath, category)
        }
    }
}