package xyz.shoaky.sourcedownloader.tagger

import xyz.shoaky.sourcedownloader.sdk.Plugin
import xyz.shoaky.sourcedownloader.sdk.PluginContext
import xyz.shoaky.sourcedownloader.sdk.PluginDescription

internal class TaggerPlugin : Plugin {
    override fun init(pluginContext: PluginContext) {
        pluginContext.registerSupplier(SimpleFileTaggerSupplier)
    }

    override fun destroy(pluginContext: PluginContext) {

    }

    override fun description(): PluginDescription {
        return PluginDescription("tagger", "0.0.1")
    }
}