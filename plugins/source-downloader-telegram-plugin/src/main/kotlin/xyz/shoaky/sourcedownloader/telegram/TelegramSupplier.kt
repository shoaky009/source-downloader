package xyz.shoaky.sourcedownloader.telegram

import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentRule
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

object TelegramSupplier : SdComponentSupplier<TelegramSource> {
    override fun apply(props: ComponentProps): TelegramSource {
        TODO("Not yet implemented")
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.source("telegram"),
            ComponentType.provider("telegram"),
            ComponentType.downloader("telegram"),
        )
    }

    override fun rules(): List<ComponentRule> {
        return listOf(
            ComponentRule.allowSource(TelegramSource::class),
            ComponentRule.allowDownloader(TelegramSource::class),
        )
    }

    override fun getComponentClass(): Class<TelegramSource> {
        return TelegramSource::class.java
    }
}