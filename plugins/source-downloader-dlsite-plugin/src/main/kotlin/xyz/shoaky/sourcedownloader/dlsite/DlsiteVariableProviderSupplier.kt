package xyz.shoaky.sourcedownloader.dlsite

import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

internal object DlsiteVariableProviderSupplier : SdComponentSupplier<DlsiteVariableProvider> {
    override fun apply(props: ComponentProps): DlsiteVariableProvider {
        return DlsiteVariableProvider()
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.provider("dlsite")
        )
    }

    override fun getComponentClass(): Class<DlsiteVariableProvider> {
        return DlsiteVariableProvider::class.java
    }


}