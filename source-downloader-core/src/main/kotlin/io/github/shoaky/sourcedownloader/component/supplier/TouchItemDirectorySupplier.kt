package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.TouchItemDirectory
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object TouchItemDirectorySupplier : ComponentSupplier<TouchItemDirectory> {

    override fun apply(props: Properties): TouchItemDirectory {
        return TouchItemDirectory
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(ComponentType.run("touch-item-directory"))
    }

    override fun autoCreateDefault(): Boolean = true

}