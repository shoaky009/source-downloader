package xyz.shoaky.sourcedownloader.component.supplier

import xyz.shoaky.sourcedownloader.component.TouchItemDirectory
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

object TouchItemDirectorySupplier : SdComponentSupplier<TouchItemDirectory> {
    override fun apply(props: Properties): TouchItemDirectory {
        return TouchItemDirectory
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(ComponentType.run("touch-item-directory"))
    }

    override fun autoCreateDefault(): Boolean = true

}