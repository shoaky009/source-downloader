package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.getchu.GetchuVariableProvider
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object GetchuVariableProviderSupplier : ComponentSupplier<GetchuVariableProvider> {

    override fun apply(context: CoreContext, props: Properties): GetchuVariableProvider {
        return GetchuVariableProvider()
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.variableProvider("getchu")
        )
    }
}