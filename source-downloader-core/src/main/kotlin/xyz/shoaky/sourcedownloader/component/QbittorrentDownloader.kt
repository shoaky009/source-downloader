package xyz.shoaky.sourcedownloader.component

import bt.metainfo.MetadataService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import xyz.shoaky.sourcedownloader.external.qbittorrent.*
import xyz.shoaky.sourcedownloader.sdk.DownloadTask
import xyz.shoaky.sourcedownloader.sdk.SourceContent
import xyz.shoaky.sourcedownloader.sdk.SourceItem
import xyz.shoaky.sourcedownloader.sdk.component.TorrentDownloader
import java.nio.file.Path
import kotlin.io.path.name

class QbittorrentDownloader(
    private val client: QbittorrentClient,
    private val alwaysDownloadAll: Boolean = false
) : TorrentDownloader {

    private val metadataService = MetadataService()

    override fun submit(task: DownloadTask) {
        val torrentsAddRequest = TorrentsAddRequest(
            listOf(task.downloadUri().toURL()),
            task.downloadPath.toString(),
            task.category,
            // 看实际效果
            false
        )
        val response = client.execute(torrentsAddRequest)
        if (QbittorrentClient.successResponse != response.body()) {
            log.error("qbittorrent submit task failed,code:${response.statusCode()} body:${response.body()}")
        }

        if (alwaysDownloadAll.not()) {
            val torrentHash = getTorrentHash(task.sourceItem)
            val torrentFiles = client.execute(TorrentFilesRequest(
                torrentHash
            )).body()

            val downloadFiles = task.downloadFiles
            val no = torrentFiles.filter {
                downloadFiles.contains(it.name).not()
            }
            log.debug("torrent:{} set prio 0 files: {}", torrentHash, no)
            client.execute(TorrentFilePrioRequest(
                torrentHash,
                no.map { it.index },
                0
            ))
        }
    }

    override fun defaultDownloadPath(): Path {
        val response = client.execute(AppGetDefaultSavePathRequest())
        if (response.statusCode() != HttpStatus.OK.value()) {
            throw RuntimeException("获取默认下载路径失败,code:${response.statusCode()} body:${response.body()}")
        }
        return Path.of(response.body())
    }

    override fun resolveFiles(sourceItem: SourceItem): List<Path> {
        val torrent = metadataService.fromUrl(sourceItem.downloadUri.toURL())
        return torrent.files
            .filter { it.size > 0 }
            .map { Path.of(it.pathElements.joinToString("/")) }
    }

    override fun isFinished(task: DownloadTask): Boolean? {
        val sourceItem = task.sourceItem
        val torrentHash = getTorrentHash(sourceItem)
        val torrent = client.execute(TorrentInfoRequest(hashes = torrentHash))
        val torrents = torrent.body()
        return torrents.map { it.progress >= 0.99f }.firstOrNull()
    }

    private fun getTorrentHash(sourceItem: SourceItem): String {
        return parseTorrentHash(sourceItem)
            ?: metadataService.fromUrl(sourceItem.downloadUri.toURL()).torrentId.toString()
    }

    override fun rename(sourceContent: SourceContent): Boolean {
        val sourceItem = sourceContent.sourceItem
        // 优化
        val torrent = metadataService.fromUrl(sourceItem.downloadUri.toURL())
        val torrentHash = torrent.torrentId.toString()
        val sourceFiles = sourceContent.sourceFiles

        val result = sourceFiles.groupBy { it.targetPath().parent }
            .map { (path, files) ->
                val setLocation = client.execute(
                    TorrentsSetLocationRequest(
                        listOf(torrentHash),
                        path.toString()
                    ))
                val movingResult = files
                    .map {
                        val sourceFileName = it.fileDownloadPath.last().name
                        val targetFileName = it.targetPath().last().name
                        val renameFile =
                            client.execute(TorrentsRenameFileRequest(torrentHash, sourceFileName, targetFileName))
                        renameFile.statusCode() == HttpStatus.OK.value()
                    }
                setLocation.statusCode() == HttpStatus.OK.value() && movingResult.all { it }
            }
        return result.all { it }
    }

    companion object {
        private val log = LoggerFactory.getLogger(QbittorrentDownloader::class.java)
    }

}

