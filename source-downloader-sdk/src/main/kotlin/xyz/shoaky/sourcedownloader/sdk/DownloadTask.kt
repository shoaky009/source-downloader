package xyz.shoaky.sourcedownloader.sdk

import java.net.URI
import java.nio.file.Path

data class DownloadTask(
    val sourceItem: SourceItem,
    val downloadFiles: List<Path>,
    val downloadPath: Path?,
    val category: String? = null,
) {

    fun downloadUri(): URI {
        return sourceItem.downloadUri
    }

    companion object {

        fun create(
            sourceItem: SourceItem,
            downloadFiles: List<Path>,
            category: String? = null,
            downloadPath: Path? = null,
        ): DownloadTask {
            return DownloadTask(
                sourceItem,
                downloadFiles,
                downloadPath,
                category)
        }
    }
}