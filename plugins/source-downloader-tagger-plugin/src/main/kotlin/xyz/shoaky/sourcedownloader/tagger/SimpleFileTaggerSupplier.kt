package xyz.shoaky.sourcedownloader.tagger

import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

internal object SimpleFileTaggerSupplier : SdComponentSupplier<SimpleFileTagger> {
    override fun apply(props: Properties): SimpleFileTagger {
        return SimpleFileTagger
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.fileTagger("simple")
        )
    }

    override fun autoCreateDefault(): Boolean = true

}