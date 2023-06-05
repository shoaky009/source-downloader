package xyz.shoaky.sourcedownloader.common.supplier

import xyz.shoaky.sourcedownloader.common.dlsite.DlsiteVariableProvider
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

internal object DlsiteVariableProviderSupplier : SdComponentSupplier<DlsiteVariableProvider> {
    override fun apply(props: Properties): DlsiteVariableProvider {
        return DlsiteVariableProvider()
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.provider("dlsite")
        )
    }

}