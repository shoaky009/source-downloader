package io.github.shoaky.sourcedownloader.common.torrent

import bt.bencoding.serializers.BEParser
import bt.metainfo.MetadataService
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.external.qbittorrent.*
import io.github.shoaky.sourcedownloader.sdk.DownloadTask
import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.ComponentException
import io.github.shoaky.sourcedownloader.sdk.component.TorrentDownloader
import io.github.shoaky.sourcedownloader.sdk.component.TorrentDownloader.Companion.tryParseTorrentHash
import io.github.shoaky.sourcedownloader.sdk.http.StatusCodes
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URL
import java.nio.file.Path
import java.security.MessageDigest
import java.util.*
import kotlin.io.path.extension

/**
 * qBittorrent下载器，支持保种和只下载需要的文件
 */
class QbittorrentDownloader(
    private val client: QbittorrentClient,
    private val alwaysDownloadAll: Boolean = false
) : TorrentDownloader {

    private val defaultDownloadPath: Path by lazy {
        loadDefaultDownloadPath()
    }

    override fun submit(task: DownloadTask): Boolean {
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
        if (QbittorrentClient.SUCCESS_RESPONSE != response.body()) {
            log.error("qbittorrent submit task failed,code:${response.statusCode()} body:${response.body()}")
            throw ComponentException.processing("qbittorrent submit task failed,code:${response.statusCode()} body:${response.body()}")
        }
        if (alwaysDownloadAll) {
            return true
        }

        val torrentHash = getInfoHashV1(task.sourceItem)
        // NOTE 必须要等一下qbittorrent(不知道有没有同步添加的方法)
        Thread.sleep(400L)

        // 可以优化暂时不知道qbittorrent是不是info hash v2优先
        // 目前如果info hash只有v1也会试一遍v2，没必要
        val (success, infoHash, body) = retryWhen({
            val fileResponse = client.execute(TorrentFilesRequest(torrentHash))
            if (fileResponse.statusCode() == StatusCodes.NOT_FOUND) {
                val infoHashV1 = getInfoHashV1(task.sourceItem, true)
                val fileResponse = client.execute(TorrentFilesRequest(infoHashV1))
                return@retryWhen Triple(
                    fileResponse.statusCode() == StatusCodes.OK,
                    infoHashV1,
                    fileResponse.body()
                )
            }
            Triple(fileResponse.statusCode() == StatusCodes.OK, torrentHash, fileResponse.body())
        }, condition = { it.first }) {
            val infoHashV2 = getInfoHashV2(task.downloadUri().toURL())
            log.info("Try to get files with info hash v2:$infoHashV2")
            val files = client.execute(TorrentFilesRequest(infoHashV2))
            Triple(files.statusCode() == StatusCodes.OK, infoHashV2, files.body())
        }

        if (success.not()) {
            throw IOException("Qbittorrent get files failed most likely because getting torrent metadata is slow, infoHash:$infoHash")
        }

        val torrentFiles = body.parseJson(jacksonTypeRef<List<TorrentFile>>())
        val relativePaths = task.relativePaths()
        val stopDownloadFiles = torrentFiles.filter {
            relativePaths.contains(it.name).not()
        }
        log.debug("torrent:{} set prio 0 files: {}", infoHash, stopDownloadFiles)
        if (stopDownloadFiles.isEmpty()) {
            return true
        }

        client.execute(
            TorrentFilePrioRequest(
                infoHash,
                stopDownloadFiles.map { it.index },
                0
            )
        )
        return true
    }

    private fun getInfoHashV2(url: URL): String {
        val content = BEParser(url).use {
            it.readMap().value["info"]?.content
                ?: throw ComponentException.processing(
                    "Try to get torrent info failed, url:${url}"
                )
        }
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(content)
        val infoHashV2 = HexFormat.of().formatHex(hashBytes).substring(0, 40)
        log.info("Get info hash v2:$infoHashV2 from url:$url")
        return infoHashV2
    }

    override fun defaultDownloadPath(): Path {
        return defaultDownloadPath
    }

    private fun loadDefaultDownloadPath(): Path {
        val response = client.execute(AppGetDefaultSavePathRequest())
        if (response.statusCode() != StatusCodes.OK) {
            throw ComponentException.processing("获取默认下载路径失败,code:${response.statusCode()} body:${response.body()}")
        }
        return Path.of(response.body())
    }

    override fun cancel(sourceItem: SourceItem, files: List<SourceFile>) {
        val torrentHash = getInfoHash(sourceItem)
        val response = client.execute(
            TorrentDeleteRequest(listOf(torrentHash))
        )
        log.info("Cancel item:{} status:{} body:{}", sourceItem, response.statusCode(), response.body())
    }

    override fun getTorrentFiles(infoHash: String): List<Path> {
        val torrentInfo =
            client.execute(TorrentInfoRequest(hashes = infoHash)).body().firstOrNull() ?: return emptyList()
        val extension = torrentInfo.contentPath.extension
        // 只有一个文件的torrent
        if (extension.isNotBlank()) {
            return listOf(torrentInfo.contentPath)
        }

        return client.execute(TorrentFilesRequest(infoHash))
            .body().parseJson(jacksonTypeRef<List<TorrentFile>>())
            .map {
                torrentInfo.contentPath.resolve(it.name)
            }
    }

    override fun isFinished(sourceItem: SourceItem): Boolean? {
        val torrentHash = getInfoHash(sourceItem)
        val torrent = client.execute(TorrentInfoRequest(hashes = torrentHash))
        val torrents = torrent.body()
        return torrents.map { it.progress >= 1.0f }.firstOrNull()
    }

    override fun move(itemContent: ItemContent): Boolean {
        val torrentHash = getInfoHash(itemContent.sourceItem)
        val sourceFiles = itemContent.fileContents

        val firstFile = sourceFiles.first()
        val saveItemFileRootDirectory = firstFile.fileSaveRootDirectory()
        val itemLocation = saveItemFileRootDirectory ?: firstFile.saveDirectoryPath()

        val allSuccess = sourceFiles.map {
            val torrentRelativePath = it.downloadPath.relativize(it.fileDownloadPath).toString()
            val targetRelativePath = itemLocation.relativize(it.targetPath()).toString()
            val renameFile = client.execute(
                TorrentsRenameFileRequest(torrentHash, torrentRelativePath, targetRelativePath)
            )
            val success = renameFile.statusCode() == StatusCodes.OK
            if (success.not()) {
                log.error("Rename file failed, hash:$torrentHash code:${renameFile.statusCode()} body:${renameFile.body()}")
            }
            success
        }.all { it }

        if (allSuccess.not()) {
            return false
        }
        val setLocationResponse = client.execute(
            TorrentsSetLocationRequest(
                listOf(torrentHash),
                itemLocation.toString()
            )
        )
        if (setLocationResponse.statusCode() != StatusCodes.OK) {
            log.error("set location failed,hash:$torrentHash code:${setLocationResponse.statusCode()} body:${setLocationResponse.body()}")
        }
        return allSuccess && setLocationResponse.statusCode() == StatusCodes.OK
    }

    private fun getInfoHash(sourceItem: SourceItem): String {
        val torrentHash = getInfoHashV1(sourceItem)
        val torrent = client.execute(TorrentInfoRequest(hashes = torrentHash))
        if (torrent.body().isEmpty()) {
            log.debug("Info hash v1 {} not found, try to get info hash v2", torrentHash)
            val infoHashV2 = getInfoHashV2(sourceItem.downloadUri.toURL())
            val response = client.execute(TorrentInfoRequest(hashes = infoHashV2))
            if (response.body().isNotEmpty()) {
                return infoHashV2
            }
            log.debug("Info hash v2 {} not found, try to get from url info hash v1", infoHashV2)
            val infoHashV1 = getInfoHashV1(sourceItem, true)
            val v1Response = client.execute(TorrentInfoRequest(hashes = infoHashV1))
            if (v1Response.statusCode() == 200) {
                return infoHashV1
            }
            log.warn("Get torrent info failed, hash:$torrentHash code:${torrent.statusCode()} body:${torrent.body()}")
        }
        return torrentHash
    }

    companion object {

        private val log = LoggerFactory.getLogger(QbittorrentDownloader::class.java)

        private fun <T> retryWhen(
            block: () -> T,
            times: Int = 3,
            condition: (T) -> Boolean,
            default: () -> T
        ): T {
            var count = 0
            while (count < times) {
                try {
                    val result = block()
                    if (condition(result)) {
                        return result
                    }
                    count++
                    log.info("retry times: $count")
                    Thread.sleep(1000L * count)
                    if (count == times) {
                        throw ComponentException.processing("max retry times")
                    }
                } catch (e: Exception) {
                    count++
                    log.info("retry times: $count")
                    Thread.sleep(1000L * count)
                    if (count == times) {
                        throw e
                    }
                }
            }
            return default.invoke()
        }
    }

}

private val metadataService = MetadataService()
fun getInfoHashV1(sourceItem: SourceItem, forceFromUrl: Boolean = false): String {
    if (forceFromUrl) {
        return metadataService.fromUrl(sourceItem.downloadUri.toURL()).torrentId.toString()
    }
    return tryParseTorrentHash(sourceItem)
        ?: metadataService.fromUrl(sourceItem.downloadUri.toURL()).torrentId.toString()
}
