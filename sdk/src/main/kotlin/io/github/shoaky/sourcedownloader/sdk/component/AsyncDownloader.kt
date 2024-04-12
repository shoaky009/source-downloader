package io.github.shoaky.sourcedownloader.sdk.component

import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.moveTo
import kotlin.io.path.name
import kotlin.io.path.notExists

/**
 * Async downloader, submit method will not wait for the download to complete before returning
 */
interface AsyncDownloader : Downloader {

    /**
     * @param sourceItem the item to check if finished
     * @return null if the task not found, otherwise return true if the task is finished
     */
    fun isFinished(sourceItem: SourceItem): Boolean?

}

/**
 * A downloader that can move files using self api
 */
interface TorrentDownloader : AsyncDownloader, FileMover {

    /**
     * replace the file with the same name in the target directory
     * @param itemContent the item to replace
     * @return true if the item is replaced
     */
    override fun replace(itemContent: ItemContent): Boolean {
        val torrentHash = lazy {
            tryParseTorrentHash(itemContent.sourceItem)?.let {
                getTorrentFiles(it)
            } ?: emptyList()
        }

        for (sourceFile in itemContent.fileContents) {
            val existTargetPath = sourceFile.existTargetPath ?: sourceFile.targetPath()
            if (existTargetPath.notExists()) {
                log.info("Replace file not exists $existTargetPath")
                move(itemContent)
                continue
            }

            val torrentFiles = torrentHash.value
            if (torrentFiles.isNotEmpty()) {
                val exists = torrentFiles.contains(sourceFile.targetPath())
                if (exists) {
                    log.info("Torrent file is same as target file $existTargetPath, skip")
                    continue
                }
            }

            val backupPath = existTargetPath.resolveSibling("${existTargetPath.name}.bak")
            existTargetPath.moveTo(backupPath)
            val targetPath = sourceFile.targetPath()

            try {
                val success = move(itemContent)
                if (success) {
                    backupPath.deleteIfExists()
                } else {
                    log.info("Move file failed $targetPath, restore from backup file $backupPath")
                    backupPath.moveTo(existTargetPath)
                }
            } catch (e: Exception) {
                if (existTargetPath.notExists()) {
                    log.info("Move file failed $targetPath, restore from backup file $backupPath")
                    backupPath.moveTo(existTargetPath)
                }
                throw e
            }
        }
        return true
    }

    /**
     * @param infoHash the torrent info hash
     * @return the paths of the torrent files, relative to the download path
     */
    fun getTorrentFiles(infoHash: String): List<Path>

    companion object {

        private val infoHashRegex = Regex("[0-9a-f]{40}")
        fun tryParseTorrentHash(sourceItem: SourceItem): String? {
            val find = infoHashRegex.find(sourceItem.downloadUri.toString())
                ?: infoHashRegex.find(sourceItem.link.toString()) ?: infoHashRegex.find(sourceItem.title)
            if (find != null) {
                return find.value
            }

            return sourceItem.attrs.asSequence()
                .filter { it.key.contains("infoHash", true) || it.key.contains("torrent", true) }
                .map { it.value }
                .filterIsInstance<String>()
                .mapNotNull { infoHashRegex.find(it)?.value }
                .firstOrNull()
        }

        private val log = LoggerFactory.getLogger(TorrentDownloader::class.java)
    }

}