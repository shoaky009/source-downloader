package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.NeverReplace
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object NeverReplaceSupplier : ComponentSupplier<NeverReplace> {

    override fun apply(context: CoreContext, props: Properties): NeverReplace {
        return NeverReplace
    }

    override fun supportNoArgs(): Boolean = true

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.fileReplacementDecider("never")
        )
    }

}