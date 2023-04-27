package xyz.shoaky.sourcedownloader.common.anitom

import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.SdComponentSupplier
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType

object AnitomVariableProviderSupplier : SdComponentSupplier<AnitomVariableProvider> {
    override fun apply(props: Properties): AnitomVariableProvider {
        return AnitomVariableProvider()
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.provider("anitom")
        )
    }
}