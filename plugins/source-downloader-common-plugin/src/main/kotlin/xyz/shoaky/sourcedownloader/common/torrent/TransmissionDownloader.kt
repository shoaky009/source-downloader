package xyz.shoaky.sourcedownloader.common.torrent

import org.slf4j.LoggerFactory
import xyz.shoaky.sourcedownloader.external.transmission.*
import xyz.shoaky.sourcedownloader.sdk.DownloadTask
import xyz.shoaky.sourcedownloader.sdk.SourceContent
import xyz.shoaky.sourcedownloader.sdk.component.TorrentDownloader
import java.nio.file.Path

class TransmissionDownloader(
    private val client: TransmissionClient,
) : TorrentDownloader {

    override fun isFinished(task: DownloadTask): Boolean? {
        val hash = getTorrentHash(task.sourceItem)
        val response = client.execute(TorrentGet(
            listOf(hash)
        ))
        val torrent = response.body().arguments.torrents.firstOrNull() ?: return null
        return torrent.percentComplete == 1.0
    }

    override fun submit(task: DownloadTask) {
        val response = client.execute(TorrentAdd(
            task.downloadUri().toString(),
            task.downloadPath,
            task.options.tags,
        ))

        val torrentHash = response.body().arguments.getHash()
        val torrent = client.execute(
            TorrentGet(listOf(torrentHash))
        ).body().arguments.torrents.firstOrNull()

        if (torrent == null) {
            log.warn("torrent:{} not found", torrentHash)
            return
        }

        val relativePaths = task.relativePaths()
        val stopDownloadFiles = torrent.files
            .mapIndexed { index, torrentFile -> index to torrentFile }
            .filter {
                relativePaths.contains(it.second.name).not()
            }

        if (stopDownloadFiles.isEmpty()) {
            return
        }
        client.execute(TorrentSet(
            listOf(torrentHash),
            stopDownloadFiles.map { it.first }
        ))
    }

    override fun defaultDownloadPath(): Path {
        return client.execute(SessionGet()).body().arguments.downloadPath
    }

    override fun rename(sourceContent: SourceContent): Boolean {
        // https://github.com/transmission/transmission/issues/3216
        // NOTE 目前Transmission的API无法完全满足命名种子内部的文件，重命名参数不能包含文件夹

        val torrentHash = getTorrentHash(sourceContent.sourceItem)
        val sourceFiles = sourceContent.sourceFiles

        val firstFile = sourceFiles.first()
        val saveItemFileRootDirectory = firstFile.itemSaveRootDirectory()
        val itemLocation = saveItemFileRootDirectory ?: firstFile.saveDirectoryPath()

        val allSuccess = sourceFiles.map {
            val torrentRelativePath = it.downloadPath.relativize(it.fileDownloadPath)

            val targetRelativePath = itemLocation.relativize(it.targetPath())
            val renameFileResponse = client.execute(
                TorrentRenamePath(listOf(torrentHash), torrentRelativePath, targetRelativePath)
            )
            val isSuccess = renameFileResponse.body().isSuccess()
            if (isSuccess.not()) {
                log.error("rename file failed,hash:$torrentHash code:${renameFileResponse.statusCode()} body:${renameFileResponse.body()}")
            }
            isSuccess
        }.all { it }

        val setLocationResponse = client.execute(
            TorrentSetLocation(
                listOf(torrentHash),
                itemLocation,
                true
            ))

        val isSuccess = setLocationResponse.body().isSuccess().not()
        if (isSuccess) {
            log.error("set location failed,hash:$torrentHash code:${setLocationResponse.statusCode()} body:${setLocationResponse.body()}")
        }
        return allSuccess && isSuccess
    }

    companion object {
        private val log = LoggerFactory.getLogger(TransmissionDownloader::class.java)
    }
}