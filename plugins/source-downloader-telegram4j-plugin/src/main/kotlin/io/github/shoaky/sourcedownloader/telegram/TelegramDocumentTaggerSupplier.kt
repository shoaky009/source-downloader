package io.github.shoaky.sourcedownloader.telegram

import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

object TelegramDocumentTaggerSupplier : SdComponentSupplier<TelegramDocumentTagger> {
    override fun apply(props: Properties): TelegramDocumentTagger {
        return TelegramDocumentTagger
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.fileTagger("telegram")
        )
    }

    override fun autoCreateDefault(): Boolean = true
}