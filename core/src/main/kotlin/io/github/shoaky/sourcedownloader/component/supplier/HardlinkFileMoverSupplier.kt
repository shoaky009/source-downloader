package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.HardlinkFileMover
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object HardlinkFileMoverSupplier : ComponentSupplier<HardlinkFileMover> {

    override fun apply(context: CoreContext, props: Properties): HardlinkFileMover {
        return HardlinkFileMover
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.fileMover("hardlink")
        )
    }

    override fun supportNoArgs(): Boolean = true

}