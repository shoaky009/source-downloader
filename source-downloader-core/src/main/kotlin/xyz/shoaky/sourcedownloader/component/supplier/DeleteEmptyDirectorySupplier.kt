package xyz.shoaky.sourcedownloader.component.supplier

import xyz.shoaky.sourcedownloader.component.DeleteEmptyDirectory
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

object DeleteEmptyDirectorySupplier : SdComponentSupplier<DeleteEmptyDirectory> {
    override fun apply(props: Properties): DeleteEmptyDirectory {
        return DeleteEmptyDirectory
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.run("delete-empty-directory")
        )
    }

    override fun autoCreateDefault(): Boolean = true
}