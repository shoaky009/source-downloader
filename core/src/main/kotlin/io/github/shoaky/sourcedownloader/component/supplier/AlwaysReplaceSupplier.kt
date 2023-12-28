package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.AlwaysReplace
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object AlwaysReplaceSupplier : ComponentSupplier<AlwaysReplace> {

    override fun apply(context: CoreContext, props: Properties): AlwaysReplace {
        return AlwaysReplace
    }

    override fun autoCreateDefault(): Boolean = true

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.fileReplacementDecider("always")
        )
    }

}