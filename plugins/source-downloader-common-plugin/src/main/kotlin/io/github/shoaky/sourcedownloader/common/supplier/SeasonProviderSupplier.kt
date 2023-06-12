package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.SeasonVariableProvider
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

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