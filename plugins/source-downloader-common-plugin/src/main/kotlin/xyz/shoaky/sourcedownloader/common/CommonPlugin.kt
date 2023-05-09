package xyz.shoaky.sourcedownloader.common

import xyz.shoaky.sourcedownloader.common.anime.AnimeFileFilterSupplier
import xyz.shoaky.sourcedownloader.common.anitom.AnitomVariableProviderSupplier
import xyz.shoaky.sourcedownloader.common.dlsite.DlsiteVariableProviderSupplier
import xyz.shoaky.sourcedownloader.common.mikan.MikanVariableProviderSupplier
import xyz.shoaky.sourcedownloader.common.rss.JackettSourceSupplier
import xyz.shoaky.sourcedownloader.common.rss.RssSourceSupplier
import xyz.shoaky.sourcedownloader.sdk.Plugin
import xyz.shoaky.sourcedownloader.sdk.PluginContext
import xyz.shoaky.sourcedownloader.sdk.PluginDescription

internal class CommonPlugin : Plugin {
    override fun init(pluginContext: PluginContext) {
        pluginContext.registerSupplier(
            AnitomVariableProviderSupplier,
            DlsiteVariableProviderSupplier,
            JackettSourceSupplier,
            RssSourceSupplier,
            AnimeFileFilterSupplier,
            MikanVariableProviderSupplier
        )
    }

    override fun destroy(pluginContext: PluginContext) {

    }

    override fun description(): PluginDescription {
        return PluginDescription("common", "0.0.1")
    }
}