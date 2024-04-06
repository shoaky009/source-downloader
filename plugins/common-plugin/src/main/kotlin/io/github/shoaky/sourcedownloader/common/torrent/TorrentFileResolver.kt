package io.github.shoaky.sourcedownloader.common.torrent

import bt.Bt
import bt.bencoding.serializers.BEParser
import bt.bencoding.types.BEList
import bt.bencoding.types.BEMap
import bt.data.file.FileSystemStorage
import bt.dht.DHTConfig
import bt.dht.DHTModule
import bt.metainfo.MetadataService
import bt.metainfo.Torrent
import bt.runtime.BtClient
import bt.runtime.BtRuntime
import bt.runtime.Config
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.ItemFileResolver
import org.slf4j.LoggerFactory
import java.net.URI
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import kotlin.io.path.Path
import kotlin.jvm.optionals.getOrNull

/**
 * Torrent文件解析器，支持磁力连接但是不推荐不太稳定
 */
object TorrentFileResolver : ItemFileResolver {

    private val metadataService = MetadataService()
    private val btStoragePath: Path = System.getenv("SOURCE_DOWNLOADER_DATA_LOCATION")?.let {
        Path(it).resolve("bittorrent")
    } ?: Path("./")

    override fun resolveFiles(sourceItem: SourceItem): List<SourceFile> {
        val downloadUri = sourceItem.downloadUri
        val torrent = if (downloadUri.scheme == "magnet") {
            val pureMagnet = removeBlankParams(downloadUri)
            // 试运行，这个不太稳
            getTorrentFromMagnet(pureMagnet)
        } else {
            try {
                metadataService.fromUrl(downloadUri.toURL())
            } catch (e: IllegalStateException) {
                log.warn("downloadUri:$downloadUri return an incorrect torrent, returning empty files", e)
                return emptyList()
            }
        }

        if (torrent.files.size == 1) {
            val metadata = torrent.source.metadata.getOrNull()
            if (metadata == null) {
                log.warn("torrent:$downloadUri metadata is null, returning empty files")
                return emptyList()
            }
            val infoMap = BEParser(metadata).use {
                (it.readMap().value["info"] as BEMap).value
            }
            val infoMapFiles = infoMap["files"] as? BEList
                ?: return torrent.files
                    .map {
                        val path = Path(it.pathElements.joinToString("/"))
                        SourceFile(path, mapOf("size" to it.size))
                    }

            val torrentName = infoMap["name"]?.toString()
            return infoMapFiles.value.filterIsInstance<BEMap>().map { file ->
                val fileMap = file.value
                val length = fileMap.getValue("length").toString().toLong()
                val fileMapPath = fileMap.getValue("path")
                val pathElements = if (fileMapPath is BEList) {
                    fileMapPath.value.map { it.toString() }
                } else {
                    listOf(fileMapPath.toString())
                }
                val path = if (torrentName == null) {
                    Path(pathElements.joinToString("/"))
                } else {
                    Path(torrentName, pathElements.joinToString("/"))
                }
                SourceFile(path, mapOf("size" to length))
            }
        }
        val parent = Path(torrent.name)
        return torrent.files
            .filter { it.size > 0 }
            .map {
                val path = parent.resolve(it.pathElements.joinToString("/"))
                SourceFile(path, mapOf("size" to it.size))
            }
    }

    private fun removeBlankParams(uri: URI): String {
        val filter = uri.schemeSpecificPart.substring(1).split("&")
            .filter { it.split("=")[1].isNotBlank() }
            .joinToString("&")
        return "magnet:?$filter"
    }

    private fun getTorrentFromMagnet(magent: String): Torrent {
        val config = Config().apply {
            numOfHashingThreads = Runtime.getRuntime().availableProcessors() * 2
        }
        val dhtModule = DHTModule(DHTConfig().apply {
            this.setShouldUseRouterBootstrap(true)
        })

        val btRuntime = BtRuntime.builder(config)
            .module(dhtModule)
            .autoLoadModules()
            .build()

        val torrentConsumer = TorrentConsumer()
        val client = Bt.client(btRuntime)
            .magnet(magent)
            .storage(FileSystemStorage(btStoragePath))
            .afterTorrentFetched(torrentConsumer)
            .stopWhenDownloaded()
            .build()
        torrentConsumer.btClient = client
        client.startAsync({
            System.err.println("Peers: " + it.connectedPeers.size)
        }, 1000)
        return torrentConsumer.get()
    }

    private val log = LoggerFactory.getLogger(TorrentFileResolver::class.java)
}

private class TorrentConsumer : Consumer<Torrent> {

    lateinit var btClient: BtClient
    private val future: CompletableFuture<Torrent> = CompletableFuture()
    override fun accept(t: Torrent) {
        future.complete(t)
    }

    fun get(timeout: Long = 60): Torrent {
        return future.orTimeout(timeout, TimeUnit.SECONDS)
            .whenComplete { _, _ ->
                btClient.stop()
            }
            .join()
    }
}