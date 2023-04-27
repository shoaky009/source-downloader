package xyz.shoaky.sourcedownloader.telegram

import it.tdlight.client.SimpleTelegramClient
import xyz.shoaky.sourcedownloader.sdk.Plugin
import xyz.shoaky.sourcedownloader.sdk.PluginContext
import xyz.shoaky.sourcedownloader.sdk.PluginDescription

internal class TelegramPlugin : Plugin {
    override fun init(pluginContext: PluginContext) {
        pluginContext.registerInstanceFactory(SimpleTelegramClientInstanceFactory)
        pluginContext.registerSupplier(TelegramSourceSupplier(pluginContext))
    }

    override fun destroy(pluginContext: PluginContext) {
        pluginContext.getInstances(SimpleTelegramClient::class.java).forEach {
            it.closeAndWait()
        }
    }

    override fun description(): PluginDescription {
        return PluginDescription("telegram", "0.0.1")
    }
}