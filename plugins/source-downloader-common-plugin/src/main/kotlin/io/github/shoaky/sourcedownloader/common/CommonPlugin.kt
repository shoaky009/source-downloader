package io.github.shoaky.sourcedownloader.common

import io.github.shoaky.sourcedownloader.common.torrent.QbittorrentClientInstanceFactory
import io.github.shoaky.sourcedownloader.common.torrent.QbittorrentDownloaderSupplier
import io.github.shoaky.sourcedownloader.sdk.Plugin
import io.github.shoaky.sourcedownloader.sdk.PluginContext
import io.github.shoaky.sourcedownloader.sdk.PluginDescription
import io.github.shoaky.sourcedownloader.sdk.util.getObjectSuppliers

internal class CommonPlugin : Plugin {
    override fun init(pluginContext: PluginContext) {
        pluginContext.registerInstanceFactory(QbittorrentClientInstanceFactory)
        pluginContext.registerSupplier(
            *getObjectSuppliers("io.github.shoaky.sourcedownloader.common.supplier"),
            QbittorrentDownloaderSupplier(pluginContext.getInstanceManager()),
        )
    }

    override fun destroy(pluginContext: PluginContext) {

    }

    override fun description(): PluginDescription {
        return PluginDescription("common", "0.0.1")
    }
}