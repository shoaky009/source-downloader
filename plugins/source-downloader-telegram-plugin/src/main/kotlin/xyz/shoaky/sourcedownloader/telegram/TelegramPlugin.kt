package xyz.shoaky.sourcedownloader.telegram

import xyz.shoaky.sourcedownloader.sdk.Plugin
import xyz.shoaky.sourcedownloader.sdk.PluginContext
import xyz.shoaky.sourcedownloader.sdk.PluginDescription

internal class TelegramPlugin : Plugin {
    override fun init(pluginContext: PluginContext) {
        // pluginContext.registerSupplier(TelegramSupplier)
    }

    override fun destroy(pluginContext: PluginContext) {

    }

    override fun description(): PluginDescription {
        return PluginDescription("telegram", "0.0.1")
    }
}