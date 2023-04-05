package xyz.shoaky.sourcedownloader.component.supplier

import xyz.shoaky.sourcedownloader.component.TouchItemDirectory
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

object TouchItemDirectorySupplier : SdComponentSupplier<TouchItemDirectory> {
    override fun apply(props: ComponentProps): TouchItemDirectory {
        return TouchItemDirectory
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(ComponentType.run("touchItemDirectory"))
    }

    override fun getComponentClass(): Class<TouchItemDirectory> {
        return TouchItemDirectory::class.java
    }

}