package xyz.shoaky.sourcedownloader.common

import xyz.shoaky.sourcedownloader.common.anime.AnimeFileFilterSupplier
import xyz.shoaky.sourcedownloader.common.anime.AnimeVariableProviderSupplier
import xyz.shoaky.sourcedownloader.common.anitom.AnitomVariableProviderSupplier
import xyz.shoaky.sourcedownloader.common.dlsite.DlsiteVariableProviderSupplier
import xyz.shoaky.sourcedownloader.common.mikan.MikanSourceSupplier
import xyz.shoaky.sourcedownloader.common.mikan.MikanVariableProviderSupplier
import xyz.shoaky.sourcedownloader.common.rss.JackettSourceSupplier
import xyz.shoaky.sourcedownloader.common.rss.RssSourceSupplier
import xyz.shoaky.sourcedownloader.common.torrent.QbittorrentClientInstanceFactory
import xyz.shoaky.sourcedownloader.common.torrent.QbittorrentDownloaderSupplier
import xyz.shoaky.sourcedownloader.common.torrent.TorrentFileResolverSupplier
import xyz.shoaky.sourcedownloader.common.torrent.TransmissionDownloaderSupplier
import xyz.shoaky.sourcedownloader.sdk.Plugin
import xyz.shoaky.sourcedownloader.sdk.PluginContext
import xyz.shoaky.sourcedownloader.sdk.PluginDescription

internal class CommonPlugin : Plugin {
    override fun init(pluginContext: PluginContext) {
        pluginContext.registerInstanceFactory(QbittorrentClientInstanceFactory)

        pluginContext.registerSupplier(
            AnitomVariableProviderSupplier,
            DlsiteVariableProviderSupplier,
            JackettSourceSupplier,
            RssSourceSupplier,
            AnimeFileFilterSupplier,
            TorrentFileResolverSupplier,
            MikanVariableProviderSupplier,
            QbittorrentDownloaderSupplier(pluginContext.getInstanceManager()),
            TransmissionDownloaderSupplier,
            MikanSourceSupplier,
            AnimeVariableProviderSupplier,
        )
    }

    override fun destroy(pluginContext: PluginContext) {

    }

    override fun description(): PluginDescription {
        return PluginDescription("common", "0.0.1")
    }
}