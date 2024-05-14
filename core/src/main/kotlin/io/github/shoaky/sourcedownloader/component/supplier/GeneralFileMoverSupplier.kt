package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.GeneralFileMover
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object GeneralFileMoverSupplier : ComponentSupplier<GeneralFileMover> {

    override fun apply(context: CoreContext, props: Properties): GeneralFileMover {
        return GeneralFileMover
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.fileMover("general")
        )
    }

    override fun supportNoArgs(): Boolean = true
}