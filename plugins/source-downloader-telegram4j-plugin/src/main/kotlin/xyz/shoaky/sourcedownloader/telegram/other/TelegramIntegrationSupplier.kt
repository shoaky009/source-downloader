package xyz.shoaky.sourcedownloader.telegram.other

import telegram4j.core.MTProtoTelegramClient
import xyz.shoaky.sourcedownloader.sdk.ComponentRule
import xyz.shoaky.sourcedownloader.sdk.PluginContext
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier
import java.nio.file.Path

class TelegramIntegrationSupplier(
    private val pluginContext: PluginContext
) : SdComponentSupplier<TelegramIntegration> {
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
            ComponentRule.allowSource(TelegramSource::class)
        )
    }
}