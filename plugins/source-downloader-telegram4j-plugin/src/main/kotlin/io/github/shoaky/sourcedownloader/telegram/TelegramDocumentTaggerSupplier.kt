package io.github.shoaky.sourcedownloader.telegram

import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object TelegramDocumentTaggerSupplier : ComponentSupplier<TelegramDocumentTagger> {

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