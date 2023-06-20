package io.github.shoaky.sourcedownloader.sdk.component

import io.github.shoaky.sourcedownloader.sdk.DownloadTask
import io.github.shoaky.sourcedownloader.sdk.SourceItem

/**
 * Async downloader, submit method will not wait for the download to complete before returning
 */
interface AsyncDownloader : Downloader {

    /**
     * @return null if the task not found, otherwise return true if the task is finished
     */
    fun isFinished(task: DownloadTask): Boolean?

}

interface TorrentDownloader : AsyncDownloader, FileMover {

    companion object {
        private val torrentHashRegex = Regex("[0-9a-f]{40}")
        fun tryParseTorrentHash(sourceItem: SourceItem): String? {
            val find = torrentHashRegex.find(sourceItem.downloadUri.toString())
                ?: torrentHashRegex.find(sourceItem.link.toString()) ?: torrentHashRegex.find(sourceItem.title)
            return find?.value
        }
    }
}