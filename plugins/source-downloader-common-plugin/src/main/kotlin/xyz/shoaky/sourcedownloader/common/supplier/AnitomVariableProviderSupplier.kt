package xyz.shoaky.sourcedownloader.common.supplier

import xyz.shoaky.sourcedownloader.common.anitom.AnitomVariableProvider
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

object AnitomVariableProviderSupplier : SdComponentSupplier<AnitomVariableProvider> {
    override fun apply(props: Properties): AnitomVariableProvider {
        return AnitomVariableProvider()
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.provider("anitom")
        )
    }

    override fun autoCreateDefault(): Boolean = true
}