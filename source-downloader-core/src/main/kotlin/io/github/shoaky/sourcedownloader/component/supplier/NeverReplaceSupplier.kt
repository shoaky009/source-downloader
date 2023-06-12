package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.NeverReplace
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

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