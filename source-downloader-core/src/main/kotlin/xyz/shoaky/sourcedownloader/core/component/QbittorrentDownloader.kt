package xyz.shoaky.sourcedownloader.core.component

import bt.metainfo.MetadataService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import xyz.shoaky.sourcedownloader.api.qbittorrent.*
import xyz.shoaky.sourcedownloader.sdk.DownloadTask
import xyz.shoaky.sourcedownloader.sdk.SourceContent
import xyz.shoaky.sourcedownloader.sdk.SourceItem
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier
import xyz.shoaky.sourcedownloader.sdk.component.TorrentDownloader
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.name

class QbittorrentDownloader(private val client: QbittorrentClient) : TorrentDownloader {

    private val metadataService = MetadataService()

    override fun submit(task: DownloadTask) {
        val torrentsAddRequest = TorrentsAddRequest(
            listOf(task.downloadURL()),
            task.downloadPath.toString(),
            task.category
        )
        val response = client.execute(torrentsAddRequest)
        if (QbittorrentClient.successResponse != response.body()) {
            log.error("qbittorrent submit task failed,code:${response.statusCode()} body:${response.body()}")
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
        val torrent = metadataService.fromUrl(sourceItem.downloadUrl)
        return torrent.files
            .filter { it.size > 0 }
            .map { Path.of(it.pathElements.joinToString("/")) }
    }

    override fun isFinished(task: DownloadTask): Boolean {
        val sourceItem = task.sourceItem
        val torrentHash = getTorrentHash(sourceItem)
        val torrent = client.execute(TorrentInfoRequest(hashes = torrentHash))
        val torrents = torrent.body()
        return torrents.map { it.progress >= 0.99f }.firstOrNull() ?: false
    }

    private fun getTorrentHash(sourceItem: SourceItem): String {
        return parseTorrentHash(sourceItem) ?: metadataService.fromUrl(sourceItem.downloadUrl).torrentId.toString()
    }

    override fun rename(sourceContent: SourceContent): Boolean {
        val sourceItem = sourceContent.sourceItem
        //优化
        val torrent = metadataService.fromUrl(sourceItem.downloadUrl)
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
                        renameFile.statusCode() == HttpStatus.OK.value() && it.targetPath().exists()
                    }
                setLocation.statusCode() == HttpStatus.OK.value() && movingResult.all { it }
            }
        return result.all { it }
    }

    companion object {
        private val log = LoggerFactory.getLogger(QbittorrentDownloader::class.java)
    }

}

object QbittorrentSupplier : SdComponentSupplier<QbittorrentDownloader> {
    override fun apply(props: ComponentProps): QbittorrentDownloader {
        val client = QbittorrentClient(props.parse())
        return QbittorrentDownloader(client)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.downloader("qbittorrent"),
            ComponentType.fileMover("qbittorrent")
        )
    }
}