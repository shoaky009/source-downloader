package xyz.shoaky.sourcedownloader.sdk

import java.net.URI
import java.nio.file.Path

data class DownloadTask(
    val sourceItem: SourceItem,
    val downloadPath: Path?,
    val category: String? = null,
) {

    fun downloadUri(): URI {
        return sourceItem.downloadUri
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