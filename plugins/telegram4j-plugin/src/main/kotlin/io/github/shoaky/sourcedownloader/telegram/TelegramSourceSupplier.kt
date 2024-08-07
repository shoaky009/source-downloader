package io.github.shoaky.sourcedownloader.telegram

import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.plugin.PluginContext

class TelegramSourceSupplier(
    private val pluginContext: PluginContext
) : ComponentSupplier<TelegramSource> {

    override fun apply(context: CoreContext, props: Properties): TelegramSource {
        val chats = props.get<List<ChatConfig>>("chats")
        val clientName = props.get<String>("client")
        val sites = props.getOrDefault<Set<String>>("sites", setOf("Telegraph"))
        val nonMedia = props.getOrDefault<Boolean>("include-non-media", false)
        val client = pluginContext.loadInstance(clientName, TelegramClientWrapper::class.java)
        return TelegramSource(TelegramMessageFetcher(client), chats, sites, nonMedia)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.source("telegram"),
        )
    }

}