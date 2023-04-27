package xyz.shoaky.sourcedownloader.component.supplier

import org.springframework.stereotype.Component
import xyz.shoaky.sourcedownloader.component.downloader.QbittorrentDownloader
import xyz.shoaky.sourcedownloader.core.InstanceManager
import xyz.shoaky.sourcedownloader.external.qbittorrent.QbittorrentClient
import xyz.shoaky.sourcedownloader.external.qbittorrent.QbittorrentConfig
import xyz.shoaky.sourcedownloader.sdk.InstanceFactory
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.SdComponentSupplier
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType

@Component
class QbittorrentDownloaderSupplier(
    private val instanceManager: InstanceManager
) : SdComponentSupplier<QbittorrentDownloader> {
    override fun apply(props: Properties): QbittorrentDownloader {
        val parse = props.parse<QbittorrentConfig>()
        val name = "qbittorrentClient:${parse.username}"
        val client = instanceManager.load(name, QbittorrentClient::class.java, props)

        // val client = instanceManager.load(clientName, QbittorrentClient::class.java)
        return QbittorrentDownloader(client, props.getOrDefault("alwaysDownloadAll", false))
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.downloader("qbittorrent"),
            ComponentType.fileMover("qbittorrent")
        )
    }

}

object QbittorrentClientInstanceFactory : InstanceFactory<QbittorrentClient> {
    override fun create(props: Properties): QbittorrentClient {
        val parse = props.parse<QbittorrentConfig>()
        return QbittorrentClient(parse)
    }

}