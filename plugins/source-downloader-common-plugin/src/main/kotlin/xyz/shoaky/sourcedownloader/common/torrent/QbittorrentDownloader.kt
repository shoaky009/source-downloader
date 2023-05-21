package xyz.shoaky.sourcedownloader.common.torrent

import bt.metainfo.MetadataService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import xyz.shoaky.sourcedownloader.external.qbittorrent.*
import xyz.shoaky.sourcedownloader.sdk.DownloadTask
import xyz.shoaky.sourcedownloader.sdk.SourceContent
import xyz.shoaky.sourcedownloader.sdk.SourceItem
import xyz.shoaky.sourcedownloader.sdk.component.TorrentDownloader
import xyz.shoaky.sourcedownloader.sdk.component.TorrentDownloader.Companion.tryParseTorrentHash
import java.nio.file.Path

class QbittorrentDownloader(
    private val client: QbittorrentClient,
    private val alwaysDownloadAll: Boolean = false
) : TorrentDownloader {

    override fun submit(task: DownloadTask) {
        val tags = task.options.tags
            .joinToString(",")
            .takeIf { it.isNotBlank() }
        val torrentsAddRequest = TorrentsAddRequest(
            listOf(task.downloadUri().toURL()),
            task.downloadPath.toString(),
            task.options.category,
            tags = tags
        )
        val response = client.execute(torrentsAddRequest)
        if (QbittorrentClient.successResponse != response.body()) {
            log.error("qbittorrent submit task failed,code:${response.statusCode()} body:${response.body()}")
            throw RuntimeException("qbittorrent submit task failed,code:${response.statusCode()} body:${response.body()}")
        }
        if (alwaysDownloadAll) {
            return
        }

        val torrentHash = getTorrentHash(task.sourceItem)
        // NOTE 必须要等一下qbittorrent(不知道有没有同步添加的方法)
        Thread.sleep(200L)
        val torrentFiles = retry({
            client.execute(TorrentFilesRequest(
                torrentHash
            )).body()
        })

        val relativePaths = task.relativePaths()
        val stopDownloadFiles = torrentFiles.filter {
            relativePaths.contains(it.name).not()
        }
        log.debug("torrent:{} set prio 0 files: {}", torrentHash, stopDownloadFiles)
        if (stopDownloadFiles.isEmpty()) {
            return
        }

        client.execute(TorrentFilePrioRequest(
            torrentHash,
            stopDownloadFiles.map { it.index },
            0
        ))
    }

    override fun defaultDownloadPath(): Path {
        val response = client.execute(AppGetDefaultSavePathRequest())
        if (response.statusCode() != HttpStatus.OK.value()) {
            throw RuntimeException("获取默认下载路径失败,code:${response.statusCode()} body:${response.body()}")
        }
        return Path.of(response.body())
    }

    override fun isFinished(task: DownloadTask): Boolean? {
        val sourceItem = task.sourceItem
        val torrentHash = getTorrentHash(sourceItem)
        val torrent = client.execute(TorrentInfoRequest(hashes = torrentHash))
        val torrents = torrent.body()
        return torrents.map { it.progress >= 0.99f }.firstOrNull()
    }

    override fun rename(sourceContent: SourceContent): Boolean {
        val torrentHash = getTorrentHash(sourceContent.sourceItem)
        val sourceFiles = sourceContent.sourceFiles

        val firstFile = sourceFiles.first()
        val saveItemFileRootDirectory = firstFile.itemSaveRootDirectory()
        val itemLocation = saveItemFileRootDirectory ?: firstFile.saveDirectoryPath()

        val allSuccess = sourceFiles.map {
            val torrentRelativePath = it.downloadPath.relativize(it.fileDownloadPath).toString()

            val targetRelativePath = itemLocation.relativize(it.targetPath()).toString()
            val renameFile = client.execute(
                TorrentsRenameFileRequest(torrentHash, torrentRelativePath, targetRelativePath)
            )
            val success = renameFile.statusCode() == HttpStatus.OK.value()
            if (success.not()) {
                log.error("rename file failed,hash:$torrentHash code:${renameFile.statusCode()} body:${renameFile.body()}")
            }
            success
        }.all { it }

        val setLocationResponse = client.execute(
            TorrentsSetLocationRequest(
                listOf(torrentHash),
                itemLocation.toString()
            ))
        if (setLocationResponse.statusCode() != HttpStatus.OK.value()) {
            log.error("set location failed,hash:$torrentHash code:${setLocationResponse.statusCode()} body:${setLocationResponse.body()}")
        }
        return allSuccess && setLocationResponse.statusCode() == HttpStatus.OK.value()
    }

    override fun exists(paths: List<Path>): Boolean {
        // TODO 尝试获取文件列表,判断是否存在
        // 目前实现的缺点，假如qBittorrent在不同的文件系统上和路径不一样，就会出现判断错误
        return super.exists(paths)
    }

    companion object {
        private val log = LoggerFactory.getLogger(QbittorrentDownloader::class.java)


        fun <T> retry(block: () -> T, times: Int = 3): T {
            var count = 0
            while (count < times) {
                try {
                    return block()
                } catch (e: Exception) {
                    count++
                    log.info("retry times: $count")
                    Thread.sleep(200L * count)
                    if (count == times) {
                        throw e
                    }
                }
            }
            throw RuntimeException("max retry times")
        }


    }

}

private val metadataService = MetadataService()
fun getTorrentHash(sourceItem: SourceItem): String {
    return tryParseTorrentHash(sourceItem)
        ?: metadataService.fromUrl(sourceItem.downloadUri.toURL()).torrentId.toString()
}
