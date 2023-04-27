package xyz.shoaky.sourcedownloader.common.dlsite

import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.SdComponentSupplier
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType

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