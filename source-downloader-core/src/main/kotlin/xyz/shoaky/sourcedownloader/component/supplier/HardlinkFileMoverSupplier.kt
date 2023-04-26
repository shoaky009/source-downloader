package xyz.shoaky.sourcedownloader.component.supplier

import xyz.shoaky.sourcedownloader.component.HardlinkFileMover
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

object HardlinkFileMoverSupplier : SdComponentSupplier<HardlinkFileMover> {
    override fun apply(props: ComponentProps): HardlinkFileMover {
        return HardlinkFileMover
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.fileMover("hardlink")
        )
    }

}