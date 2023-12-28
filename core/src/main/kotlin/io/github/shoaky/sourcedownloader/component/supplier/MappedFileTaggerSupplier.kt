package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.MappedFileTagger
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object MappedFileTaggerSupplier : ComponentSupplier<MappedFileTagger> {

    override fun apply(context: CoreContext, props: Properties): MappedFileTagger {
        return MappedFileTagger(props.get("mapping"))
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.fileTagger("mapped")
        )
    }
}