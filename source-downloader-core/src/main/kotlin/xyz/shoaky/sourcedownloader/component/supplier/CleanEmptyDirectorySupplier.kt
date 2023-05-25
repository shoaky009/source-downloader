package xyz.shoaky.sourcedownloader.component.supplier

import xyz.shoaky.sourcedownloader.component.CleanEmptyDirectory
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

object CleanEmptyDirectorySupplier : SdComponentSupplier<CleanEmptyDirectory> {

    override fun apply(props: Properties): CleanEmptyDirectory {
        return CleanEmptyDirectory
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.run("clean-empty-directory")
        )
    }

    override fun autoCreateDefault(): Boolean = true
}