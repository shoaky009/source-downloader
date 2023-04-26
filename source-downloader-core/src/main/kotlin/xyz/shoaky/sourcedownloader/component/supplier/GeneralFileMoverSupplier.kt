package xyz.shoaky.sourcedownloader.component.supplier

import xyz.shoaky.sourcedownloader.component.GeneralFileMover
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.FileMover
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

object GeneralFileMoverSupplier : SdComponentSupplier<GeneralFileMover> {
    override fun apply(props: ComponentProps): GeneralFileMover {
        return GeneralFileMover
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType("general", FileMover::class)
        )
    }

}