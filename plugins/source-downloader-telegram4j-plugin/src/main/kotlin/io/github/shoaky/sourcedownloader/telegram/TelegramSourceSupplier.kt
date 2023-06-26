package io.github.shoaky.sourcedownloader.telegram

import io.github.shoaky.sourcedownloader.sdk.PluginContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentRule
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.SdComponentSupplier
import telegram4j.core.MTProtoTelegramClient

class TelegramSourceSupplier(
    private val pluginContext: PluginContext
) : SdComponentSupplier<TelegramSource> {
    override fun apply(props: Properties): TelegramSource {
        val chats = props.get<List<ChatConfig>>("chats")
        val clientName = props.get<String>("client")
        val client = pluginContext.load(clientName, MTProtoTelegramClient::class.java)
        return TelegramSource(DefaultMessageFetcher(client), chats)
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