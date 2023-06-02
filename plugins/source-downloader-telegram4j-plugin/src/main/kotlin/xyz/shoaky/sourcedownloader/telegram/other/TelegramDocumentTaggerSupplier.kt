package xyz.shoaky.sourcedownloader.telegram.other

import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

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