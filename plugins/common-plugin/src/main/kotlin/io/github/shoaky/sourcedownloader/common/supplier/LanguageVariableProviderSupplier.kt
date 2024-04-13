package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.LanguageVariableProvider
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object LanguageVariableProviderSupplier : ComponentSupplier<LanguageVariableProvider> {

    override fun apply(context: CoreContext, props: Properties): LanguageVariableProvider {
        val readContent = props.getOrDefault("read-content", false)
        return LanguageVariableProvider(readContent)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.variableProvider("language")
        )
    }

    override fun supportNoArgs(): Boolean = true
}