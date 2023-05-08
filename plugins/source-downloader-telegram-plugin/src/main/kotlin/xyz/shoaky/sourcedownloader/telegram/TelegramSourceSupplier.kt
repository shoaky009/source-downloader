package xyz.shoaky.sourcedownloader.telegram

import it.tdlight.client.SimpleTelegramClient
import xyz.shoaky.sourcedownloader.sdk.ComponentRule
import xyz.shoaky.sourcedownloader.sdk.PluginContext
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

class TelegramSourceSupplier(
    private val pluginContext: PluginContext
) : SdComponentSupplier<TelegramSource> {
    override fun apply(props: Properties): TelegramSource {
        val chatId = props.get<Long>("chatId")
        val clientName = props.get<String>("client")
        val client = pluginContext.load(clientName, SimpleTelegramClient::class.java)
        return TelegramSource(client, chatId)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.source("telegram"),
        )
    }

    override fun rules(): List<ComponentRule> {
        return listOf(
            ComponentRule.allowDownloader(TelegramSource::class),
        )
    }

}