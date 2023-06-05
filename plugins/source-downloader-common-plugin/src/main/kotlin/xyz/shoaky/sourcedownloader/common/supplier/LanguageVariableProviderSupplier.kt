package xyz.shoaky.sourcedownloader.common.supplier

import xyz.shoaky.sourcedownloader.common.LanguageVariableProvider
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

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