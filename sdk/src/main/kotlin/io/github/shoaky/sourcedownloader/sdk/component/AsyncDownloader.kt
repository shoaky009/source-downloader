package io.github.shoaky.sourcedownloader.sdk.component

import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import org.slf4j.LoggerFactory
import kotlin.io.path.deleteIfExists
import kotlin.io.path.moveTo
import kotlin.io.path.name
import kotlin.io.path.notExists

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
        for (sourceFile in itemContent.sourceFiles) {
            val existTargetPath = sourceFile.existTargetPath ?: sourceFile.targetPath()
            if (existTargetPath.notExists()) {
                log.info("Replace file not exists $existTargetPath")
                move(itemContent)
                continue
            }

            val backupPath = existTargetPath.resolveSibling("${existTargetPath.name}.bak")
            existTargetPath.moveTo(backupPath)
            val targetPath = sourceFile.targetPath()

            try {
                val success = move(itemContent)
                if (success) {
                    backupPath.deleteIfExists()
                } else {
                    log.error("Move file failed $targetPath, restore from backup file $backupPath")
                    backupPath.moveTo(existTargetPath)
                }
            } catch (e: Exception) {
                if (existTargetPath.notExists()) {
                    log.error("Move file failed $targetPath, restore from backup file $backupPath")
                    backupPath.moveTo(existTargetPath)
                }
                throw e
            }
        }
        return true
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