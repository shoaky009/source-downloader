package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.GeneralFileMover
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.FileMover

object GeneralFileMoverSupplier : ComponentSupplier<GeneralFileMover> {

    override fun apply(props: Properties): GeneralFileMover {
        return GeneralFileMover
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType("general", FileMover::class)
        )
    }

    override fun autoCreateDefault(): Boolean = true
}