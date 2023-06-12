package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.LanguageVariableProvider
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

object LanguageVariableProviderSupplier : SdComponentSupplier<LanguageVariableProvider> {
    override fun apply(props: Properties): LanguageVariableProvider {
        return LanguageVariableProvider
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.provider("language")
        )
    }

    override fun autoCreateDefault(): Boolean = true
}