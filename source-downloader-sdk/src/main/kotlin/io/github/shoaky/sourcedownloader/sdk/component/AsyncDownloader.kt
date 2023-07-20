package io.github.shoaky.sourcedownloader.sdk.component

import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import kotlin.io.path.deleteIfExists

/**
 * Async downloader, submit method will not wait for the download to complete before returning
 */
interface AsyncDownloader : Downloader {

    /**
     * @return null if the task not found, otherwise return true if the task is finished
     */
    fun isFinished(sourceItem: SourceItem): Boolean?

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

    /**
     * 偷懒的实现，后面可能要调整
     */
    override fun replace(itemContent: ItemContent): Boolean {
        val fileContent = itemContent.sourceFiles.first()
        fileContent.targetPath().deleteIfExists()
        return move(itemContent)
    }
}