package io.github.shoaky.sourcedownloader.common.torrent

import io.github.shoaky.sourcedownloader.external.transmission.*
import io.github.shoaky.sourcedownloader.sdk.DownloadTask
import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.TorrentDownloader
import org.slf4j.LoggerFactory
import java.nio.file.Path

/**
 * Transmission下载器，文件移动不支持保种，建议使用硬链接文件移动器
 */
class TransmissionDownloader(
    private val client: TransmissionClient,
) : TorrentDownloader {

    override fun isFinished(sourceItem: SourceItem): Boolean? {
        val hash = getTorrentHash(sourceItem)
        val response = client.execute(
            TorrentGet(
                listOf(hash)
            )
        )
        val torrent = response.body().arguments.torrents.firstOrNull() ?: return null
        return torrent.percentComplete == 1.0
    }

    override fun submit(task: DownloadTask): Boolean {
        val response = client.execute(
            TorrentAdd(
                task.downloadUri().toString(),
                task.downloadPath,
                task.options.tags,
            )
        )

        val torrentHash = response.body().arguments.getHash()
        val torrent = client.execute(
            TorrentGet(listOf(torrentHash))
        ).body().arguments.torrents.firstOrNull()

        if (torrent == null) {
            log.warn("torrent:{} not found", torrentHash)
            return true
        }

        val relativePaths = task.relativePaths()
        val stopDownloadFiles = torrent.files
            .mapIndexed { index, torrentFile -> index to torrentFile }
            .filter {
                relativePaths.contains(it.second.name).not()
            }

        if (stopDownloadFiles.isEmpty()) {
            return true
        }
        client.execute(TorrentSet(
            listOf(torrentHash),
            stopDownloadFiles.map { it.first }
        ))
        return true
    }

    override fun defaultDownloadPath(): Path {
        return client.execute(SessionGet()).body().arguments.downloadPath
    }

    override fun cancel(sourceItem: SourceItem, files: List<SourceFile>) {
        val hash = getTorrentHash(sourceItem)
        val response = client.execute(TorrentDelete(listOf(hash)))
        log.info("cancel item:{} status:{} body:{}", sourceItem, response.statusCode(), response.body())
    }

    override fun move(itemContent: ItemContent): Boolean {
        // https://github.com/transmission/transmission/issues/3216
        // NOTE 目前Transmission的API无法完全满足命名种子内部的文件，重命名参数不能包含文件夹
        val torrentHash = getTorrentHash(itemContent.sourceItem)
        val sourceFiles = itemContent.sourceFiles

        val grouping = sourceFiles.groupBy { it.targetPath().parent }
        if (grouping.size > 1) {
            log.warn("TargetPaths has multiple parent paths, but transmission doesn't support them.")
        }
        val (location, files) = grouping.maxBy { it.value.size }
        val allSuccess = files.map {
            val torrentRelativePath = it.downloadPath.relativize(it.fileDownloadPath)
            val targetPath = it.targetPath()
            // 只能改文件名
            val renameFileResponse = client.execute(
                TorrentRenamePath(listOf(torrentHash), torrentRelativePath, targetPath.fileName)
            )

            val isSuccess = renameFileResponse.body().isSuccess()
            if (isSuccess.not()) {
                log.error("Rename file failed, hash:$torrentHash code:${renameFileResponse.statusCode()} body:${renameFileResponse.body()}")
            }
            isSuccess
        }.all { it }

        val setLocationResponse = client.execute(
            TorrentSetLocation(listOf(torrentHash), location, true)
        )

        val isSuccess = setLocationResponse.body().isSuccess().not()
        if (isSuccess) {
            log.error("Set location failed,hash:$torrentHash code:${setLocationResponse.statusCode()} body:${setLocationResponse.body()}")
        }
        return allSuccess && isSuccess
    }

    companion object {

        private val log = LoggerFactory.getLogger(TransmissionDownloader::class.java)
    }
}