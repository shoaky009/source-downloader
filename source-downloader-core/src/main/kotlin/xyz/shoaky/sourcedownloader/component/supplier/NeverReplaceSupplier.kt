package xyz.shoaky.sourcedownloader.component.supplier

import xyz.shoaky.sourcedownloader.component.NeverReplace
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

object NeverReplaceSupplier : SdComponentSupplier<NeverReplace> {
    override fun apply(props: Properties): NeverReplace {
        return NeverReplace
    }

    override fun autoCreateDefault(): Boolean = true

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.replacementDecider("never")
        )
    }

}