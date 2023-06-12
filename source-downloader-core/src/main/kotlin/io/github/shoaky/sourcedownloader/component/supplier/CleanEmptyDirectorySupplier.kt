package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.CleanEmptyDirectory
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

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