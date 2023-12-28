package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.SeasonVariableProvider
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object SeasonVariableProviderSupplier : ComponentSupplier<SeasonVariableProvider> {

    override fun apply(context: CoreContext, props: Properties): SeasonVariableProvider {
        return SeasonVariableProvider
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.variableProvider("season")
        )
    }

    override fun autoCreateDefault(): Boolean = true
}