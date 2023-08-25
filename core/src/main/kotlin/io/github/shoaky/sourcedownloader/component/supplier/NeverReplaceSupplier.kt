package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.NeverReplace
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object NeverReplaceSupplier : ComponentSupplier<NeverReplace> {

    override fun apply(props: Properties): NeverReplace {
        return NeverReplace
    }

    override fun autoCreateDefault(): Boolean = true

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.fileReplacementDecider("never")
        )
    }

}