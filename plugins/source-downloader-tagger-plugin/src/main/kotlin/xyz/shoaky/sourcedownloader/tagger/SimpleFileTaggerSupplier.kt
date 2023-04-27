package xyz.shoaky.sourcedownloader.tagger

import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.SdComponentSupplier
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType

internal object SimpleFileTaggerSupplier : SdComponentSupplier<SimpleFileTagger> {
    override fun apply(props: Properties): SimpleFileTagger {
        return SimpleFileTagger
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.fileTagger("simple")
        )
    }

}