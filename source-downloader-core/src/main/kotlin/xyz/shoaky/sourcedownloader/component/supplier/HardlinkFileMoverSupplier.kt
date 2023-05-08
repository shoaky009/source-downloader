package xyz.shoaky.sourcedownloader.component.supplier

import xyz.shoaky.sourcedownloader.component.HardlinkFileMover
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

object HardlinkFileMoverSupplier : SdComponentSupplier<HardlinkFileMover> {
    override fun apply(props: Properties): HardlinkFileMover {
        return HardlinkFileMover
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.fileMover("hardlink")
        )
    }

    override fun autoCreateDefault(): Boolean = true

}