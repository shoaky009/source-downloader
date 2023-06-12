package io.github.shoaky.sourcedownloader.telegram

import it.tdlight.client.SimpleTelegramClient
import io.github.shoaky.sourcedownloader.sdk.Plugin
import io.github.shoaky.sourcedownloader.sdk.PluginContext
import io.github.shoaky.sourcedownloader.sdk.PluginDescription

internal class TelegramPlugin : Plugin {
    override fun init(pluginContext: PluginContext) {
        pluginContext.registerInstanceFactory(TelegramClientInstanceFactory(pluginContext.getPersistentDataPath()))
        pluginContext.registerSupplier(TelegramSourceSupplier(pluginContext))
    }

    override fun destroy(pluginContext: PluginContext) {
        val instanceManager = pluginContext.getInstanceManager()
        instanceManager.getInstance(SimpleTelegramClient::class.java).forEach {
            it.closeAndWait()
        }
    }

    override fun description(): PluginDescription {
        return PluginDescription("telegram", "0.0.1")
    }
}