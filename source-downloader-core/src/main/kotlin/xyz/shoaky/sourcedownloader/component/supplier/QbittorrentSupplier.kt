package xyz.shoaky.sourcedownloader.component.supplier

import xyz.shoaky.sourcedownloader.component.QbittorrentDownloader
import xyz.shoaky.sourcedownloader.external.qbittorrent.QbittorrentClient
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

object QbittorrentSupplier : SdComponentSupplier<QbittorrentDownloader> {
    override fun apply(props: ComponentProps): QbittorrentDownloader {
        val client = QbittorrentClient(props.parse())
        return QbittorrentDownloader(client, props.getOrDefault("alwaysDownloadAll", false))
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.downloader("qbittorrent"),
            ComponentType.fileMover("qbittorrent")
        )
    }

    override fun getComponentClass(): Class<QbittorrentDownloader> {
        return QbittorrentDownloader::class.java
    }
}