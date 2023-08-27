package io.github.shoaky.sourcedownloader.common.torrent

import io.github.shoaky.sourcedownloader.external.qbittorrent.QbittorrentClient
import io.github.shoaky.sourcedownloader.external.qbittorrent.QbittorrentConfig
import io.github.shoaky.sourcedownloader.sdk.InstanceFactory
import io.github.shoaky.sourcedownloader.sdk.InstanceManager
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentRule
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

class QbittorrentDownloaderSupplier(
    private val instanceManager: InstanceManager
) : ComponentSupplier<QbittorrentDownloader> {

    override fun apply(props: Properties): QbittorrentDownloader {
        val parse = props.parse<QbittorrentConfig>()
        val name = "qbittorrentClient:${parse.username}"
        val client = instanceManager.load(name, QbittorrentClient::class.java, props)

        // val client = instanceManager.load(clientName, QbittorrentClient::class.java)
        return QbittorrentDownloader(client, props.getOrDefault("always-download-all", false))
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

    override fun type(): Class<QbittorrentClient> {
        return QbittorrentClient::class.java
    }

}