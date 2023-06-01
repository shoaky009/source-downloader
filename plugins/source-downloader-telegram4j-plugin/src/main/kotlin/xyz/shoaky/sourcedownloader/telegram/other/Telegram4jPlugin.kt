package xyz.shoaky.sourcedownloader.telegram.other

import telegram4j.core.MTProtoTelegramClient
import xyz.shoaky.sourcedownloader.sdk.Plugin
import xyz.shoaky.sourcedownloader.sdk.PluginContext
import xyz.shoaky.sourcedownloader.sdk.PluginDescription

class Telegram4jPlugin : Plugin {
    override fun init(pluginContext: PluginContext) {
        pluginContext.registerInstanceFactory(TelegramClientInstanceFactory)
        pluginContext.registerSupplier(TelegramSourceSupplier(pluginContext))
    }

    override fun destroy(pluginContext: PluginContext) {
        val instanceManager = pluginContext.getInstanceManager()
        instanceManager.getInstance(MTProtoTelegramClient::class.java).forEach {
            it.disconnect().subscribe()
        }
    }

    override fun description(): PluginDescription {
        return PluginDescription("telegram4j", "0.0.1")
    }
}