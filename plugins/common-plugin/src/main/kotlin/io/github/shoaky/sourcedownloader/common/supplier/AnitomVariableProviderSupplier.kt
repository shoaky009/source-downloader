package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.anitom.AnitomVariableProvider
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object AnitomVariableProviderSupplier : ComponentSupplier<AnitomVariableProvider> {

    override fun apply(context: CoreContext, props: Properties): AnitomVariableProvider {
        return AnitomVariableProvider()
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.variableProvider("anitom")
        )
    }

    override fun autoCreateDefault(): Boolean = true
}