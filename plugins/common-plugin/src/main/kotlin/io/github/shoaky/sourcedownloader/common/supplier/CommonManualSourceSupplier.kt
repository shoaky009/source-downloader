package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.CommonManualSource
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object CommonManualSourceSupplier : ComponentSupplier<CommonManualSource> {

    override fun apply(context: CoreContext, props: Properties): CommonManualSource {
        return CommonManualSource
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.manualSource("common")
        )
    }

    override fun supportNoArgs(): Boolean = true
}