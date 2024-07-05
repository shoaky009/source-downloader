package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.anime.ChiiVariableProvider
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object ChiiVariableProviderSupplier : ComponentSupplier<ChiiVariableProvider> {

    override fun apply(context: CoreContext, props: Properties): ChiiVariableProvider {
        return ChiiVariableProvider()
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(ComponentType.variableProvider("chii"))
    }

    override fun supportNoArgs(): Boolean = true
}