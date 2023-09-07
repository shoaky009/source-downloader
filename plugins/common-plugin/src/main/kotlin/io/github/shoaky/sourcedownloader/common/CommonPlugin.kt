package io.github.shoaky.sourcedownloader.common

import io.github.shoaky.sourcedownloader.common.anime.BgmTvClientInstanceFactory
import io.github.shoaky.sourcedownloader.common.supplier.AnimeVariableProviderSupplier
import io.github.shoaky.sourcedownloader.common.supplier.MikanVariableProviderSupplier
import io.github.shoaky.sourcedownloader.common.torrent.QbittorrentClientInstanceFactory
import io.github.shoaky.sourcedownloader.common.torrent.QbittorrentDownloaderSupplier
import io.github.shoaky.sourcedownloader.sdk.plugin.Plugin
import io.github.shoaky.sourcedownloader.sdk.plugin.PluginContext
import io.github.shoaky.sourcedownloader.sdk.plugin.PluginDescription
import io.github.shoaky.sourcedownloader.sdk.util.getObjectSuppliers

internal class CommonPlugin : Plugin {

    override fun init(pluginContext: PluginContext) {
        pluginContext.registerInstanceFactory(
            QbittorrentClientInstanceFactory,
            BgmTvClientInstanceFactory,
        )
        pluginContext.registerSupplier(
            *getObjectSuppliers("io.github.shoaky.sourcedownloader.common.supplier"),
            QbittorrentDownloaderSupplier(pluginContext.getInstanceManager()),
            AnimeVariableProviderSupplier(pluginContext),
            MikanVariableProviderSupplier(pluginContext),
        )
    }

    override fun destroy(pluginContext: PluginContext) {

    }

    override fun description(): PluginDescription {
        return PluginDescription("common", "0.0.1")
    }
}