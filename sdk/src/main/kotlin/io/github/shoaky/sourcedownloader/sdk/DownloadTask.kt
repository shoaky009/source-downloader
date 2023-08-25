package io.github.shoaky.sourcedownloader.sdk

import java.net.URI
import java.nio.file.Path

data class DownloadTask(
    val sourceItem: SourceItem,
    /**
     * The absolute path of the file to download.
     */
    val downloadFiles: List<SourceFile>,
    /**
     * The absolute path of the directory.
     */
    val downloadPath: Path,
    val options: DownloadOptions = DownloadOptions(),
) {

    fun downloadUri(): URI {
        return sourceItem.downloadUri
    }

    fun relativePaths(): List<Path> {
        return downloadFiles.map { it.path }.map {
            if (it.isAbsolute) {
                downloadPath.relativize(it)
            } else {
                it
            }
        }
    }
}