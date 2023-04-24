package xyz.shoaky.sourcedownloader.tagger

import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

internal object SimpleFileTaggerSupplier : SdComponentSupplier<SimpleFileTagger> {
    override fun apply(props: ComponentProps): SimpleFileTagger {
        return SimpleFileTagger
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.fileTagger("simple")
        )
    }

    override fun getComponentClass(): Class<SimpleFileTagger> {
        return SimpleFileTagger::class.java
    }

}