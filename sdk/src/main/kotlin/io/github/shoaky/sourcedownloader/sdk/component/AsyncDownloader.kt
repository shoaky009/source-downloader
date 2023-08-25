package io.github.shoaky.sourcedownloader.sdk.component

import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import org.slf4j.LoggerFactory
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.moveTo
import kotlin.io.path.name

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

    /**
     * 容器化时需要和下载器的路径对齐，否则会出现文件找不到的问题
     */
    override fun replace(itemContent: ItemContent): Boolean {
        val fileContent = itemContent.sourceFiles.first()
        val targetPath = fileContent.targetPath()
        val backupPath = targetPath.resolveSibling("${targetPath.name}.bak")
        targetPath.moveTo(backupPath)
        try {
            val move = move(itemContent)
            backupPath.deleteIfExists()
            return move
        } catch (e: Exception) {
            if (targetPath.exists().not()) {
                log.error("Move file failed $targetPath, restore from backup file $backupPath")
                backupPath.moveTo(targetPath)
            }
            throw e
        }
    }

    companion object {

        private val torrentHashRegex = Regex("[0-9a-f]{40}")
        fun tryParseTorrentHash(sourceItem: SourceItem): String? {
            val find = torrentHashRegex.find(sourceItem.downloadUri.toString())
                ?: torrentHashRegex.find(sourceItem.link.toString()) ?: torrentHashRegex.find(sourceItem.title)
            return find?.value
        }

        private val log = LoggerFactory.getLogger(TorrentDownloader::class.java)
    }

}