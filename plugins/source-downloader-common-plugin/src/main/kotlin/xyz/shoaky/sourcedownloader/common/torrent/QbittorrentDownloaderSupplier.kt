package xyz.shoaky.sourcedownloader.common.torrent

import xyz.shoaky.sourcedownloader.external.qbittorrent.QbittorrentClient
import xyz.shoaky.sourcedownloader.external.qbittorrent.QbittorrentConfig
import xyz.shoaky.sourcedownloader.sdk.ComponentRule
import xyz.shoaky.sourcedownloader.sdk.InstanceFactory
import xyz.shoaky.sourcedownloader.sdk.InstanceManager
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

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
            ComponentType.fileMover("qbittorrent"),
        )
    }

    override fun rules(): List<ComponentRule> {
        return listOf(
            ComponentRule.allowFileResolver(TorrentFileResolver::class)
        )
    }
}

object QbittorrentClientInstanceFactory : InstanceFactory<QbittorrentClient> {
    override fun create(props: Properties): QbittorrentClient {
        val parse = props.parse<QbittorrentConfig>()
        return QbittorrentClient(parse)
    }

}