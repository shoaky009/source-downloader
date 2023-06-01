package xyz.shoaky.sourcedownloader.telegram.other

import telegram4j.core.MTProtoTelegramClient
import xyz.shoaky.sourcedownloader.sdk.ComponentRule
import xyz.shoaky.sourcedownloader.sdk.PluginContext
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

class TelegramSourceSupplier(
    private val pluginContext: PluginContext
) : SdComponentSupplier<TelegramSource> {
    override fun apply(props: Properties): TelegramSource {
        val chatIds = props.get<List<Long>>("chat-ids")
        val clientName = props.get<String>("client")
        val client = pluginContext.load(clientName, MTProtoTelegramClient::class.java)
        return TelegramSource(client, chatIds)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.source("telegram4j"),
        )
    }

    override fun rules(): List<ComponentRule> {
        return listOf(
            ComponentRule.allowDownloader(TelegramSource::class),
        )
    }

}