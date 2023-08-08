package io.github.shoaky.sourcedownloader.common.torrent

import bt.Bt
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
import java.net.URI
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import kotlin.io.path.Path

/**
 * Torrent文件解析器，支持磁力连接但是不推荐不太稳定
 */
object TorrentFileResolver : ItemFileResolver {

    private val metadataService = MetadataService()
    override fun resolveFiles(sourceItem: SourceItem): List<SourceFile> {
        val downloadUri = sourceItem.downloadUri
        val torrent = if (downloadUri.scheme == "magnet") {
            val pureMagnet = removeBlankParams(downloadUri)
            // 试运行，这个不太稳
            getTorrentFromMagnet(pureMagnet)
        } else {
            metadataService.fromUrl(downloadUri.toURL())
        }

        if (torrent.files.size == 1) {
            return torrent.files
                .map {
                    val path = Path(it.pathElements.joinToString("/"))
                    SourceFile(path, mapOf("size" to it.size))
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
            // TODO 后面再改
            .storage(FileSystemStorage(Path("./")))
            .afterTorrentFetched(torrentConsumer)
            .stopWhenDownloaded()
            .build()
        torrentConsumer.btClient = client
        client.startAsync({
            System.err.println("Peers: " + it.connectedPeers.size)
        }, 1000)
        return torrentConsumer.get()
    }

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