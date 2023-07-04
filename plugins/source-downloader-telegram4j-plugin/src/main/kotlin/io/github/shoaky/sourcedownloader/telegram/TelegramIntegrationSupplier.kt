package io.github.shoaky.sourcedownloader.telegram

import io.github.shoaky.sourcedownloader.sdk.PluginContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentRule
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import telegram4j.core.MTProtoTelegramClient
import java.nio.file.Path

class TelegramIntegrationSupplier(
    private val pluginContext: PluginContext
) : ComponentSupplier<TelegramIntegration> {

    override fun apply(props: Properties): TelegramIntegration {
        val clientName = props.get<String>("client")
        val downloadPath = props.get<Path>("download-path")
        val load = pluginContext.getInstanceManager().load(clientName, MTProtoTelegramClient::class.java)
        return TelegramIntegration(load, downloadPath)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.provider("telegram"),
            ComponentType.fileResolver("telegram"),
            ComponentType.downloader("telegram"),
        )
    }

    override fun rules(): List<ComponentRule> {
        return listOf(
            ComponentRule.allowSource(TelegramSource::class),
            ComponentRule.allowFileResolver(TelegramIntegration::class),
            ComponentRule.allowDownloader(TelegramIntegration::class),
        )
    }
}