package io.github.shoaky.sourcedownloader.telegram

import io.github.shoaky.sourcedownloader.sdk.plugin.Plugin
import io.github.shoaky.sourcedownloader.sdk.plugin.PluginContext
import io.github.shoaky.sourcedownloader.sdk.plugin.PluginDescription
import telegram4j.core.MTProtoTelegramClient

class Telegram4jPlugin : Plugin {
    override fun init(pluginContext: PluginContext) {
        pluginContext.registerInstanceFactory(TelegramClientInstanceFactory)
        pluginContext.registerSupplier(
            TelegramSourceSupplier(pluginContext),
            TelegramIntegrationSupplier(pluginContext),
            TelegramMediaTaggerSupplier
        )
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