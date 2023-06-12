package io.github.shoaky.sourcedownloader.sdk

import java.net.URI
import java.nio.file.Path

data class DownloadTask(
    val sourceItem: SourceItem,
    val downloadFiles: List<Path>,
    val downloadPath: Path,
    val options: DownloadOptions = DownloadOptions(),
) {

    fun downloadUri(): URI {
        return sourceItem.downloadUri
    }

    fun relativePaths(): List<Path> {
        return downloadFiles.map {
            if (it.isAbsolute) {
                downloadPath.relativize(it)
            } else {
                it
            }
        }
    }
}