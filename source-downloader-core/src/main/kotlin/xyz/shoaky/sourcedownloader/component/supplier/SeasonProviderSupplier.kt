package xyz.shoaky.sourcedownloader.component.supplier

import xyz.shoaky.sourcedownloader.component.provider.SeasonVariableProvider
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

object SeasonProviderSupplier : SdComponentSupplier<SeasonVariableProvider> {
    override fun apply(props: Properties): SeasonVariableProvider {
        return SeasonVariableProvider
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.provider("season")
        )
    }

    override fun autoCreateDefault(): Boolean = true
}