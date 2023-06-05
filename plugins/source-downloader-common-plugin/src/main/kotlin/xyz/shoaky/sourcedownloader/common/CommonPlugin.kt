package xyz.shoaky.sourcedownloader.common

import xyz.shoaky.sourcedownloader.common.torrent.QbittorrentClientInstanceFactory
import xyz.shoaky.sourcedownloader.common.torrent.QbittorrentDownloaderSupplier
import xyz.shoaky.sourcedownloader.sdk.Plugin
import xyz.shoaky.sourcedownloader.sdk.PluginContext
import xyz.shoaky.sourcedownloader.sdk.PluginDescription
import xyz.shoaky.sourcedownloader.sdk.util.getObjectSuppliers

internal class CommonPlugin : Plugin {
    override fun init(pluginContext: PluginContext) {
        pluginContext.registerInstanceFactory(QbittorrentClientInstanceFactory)
        pluginContext.registerSupplier(
            *getObjectSuppliers("xyz.shoaky.sourcedownloader.common.supplier"),
            QbittorrentDownloaderSupplier(pluginContext.getInstanceManager()),
        )
    }

    override fun destroy(pluginContext: PluginContext) {

    }

    override fun description(): PluginDescription {
        return PluginDescription("common", "0.0.1")
    }
}