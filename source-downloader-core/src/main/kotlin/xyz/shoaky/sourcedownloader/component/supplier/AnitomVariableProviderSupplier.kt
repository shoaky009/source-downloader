package xyz.shoaky.sourcedownloader.component.supplier

import xyz.shoaky.sourcedownloader.component.provider.AnitomVariableProvider
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

object AnitomVariableProviderSupplier : SdComponentSupplier<AnitomVariableProvider> {
    override fun apply(props: ComponentProps): AnitomVariableProvider {
        return AnitomVariableProvider()
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.provider("anitom")
        )
    }

    override fun getComponentClass(): Class<AnitomVariableProvider> {
        return AnitomVariableProvider::class.java
    }
}