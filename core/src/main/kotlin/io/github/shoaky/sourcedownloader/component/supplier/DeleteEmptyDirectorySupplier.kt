package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.DeleteEmptyDirectory
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object DeleteEmptyDirectorySupplier : ComponentSupplier<DeleteEmptyDirectory> {

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