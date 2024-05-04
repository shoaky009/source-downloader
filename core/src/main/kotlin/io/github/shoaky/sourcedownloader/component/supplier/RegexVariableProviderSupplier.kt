package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.provider.RegexVariable
import io.github.shoaky.sourcedownloader.component.provider.RegexVariableProvider
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object RegexVariableProviderSupplier : ComponentSupplier<RegexVariableProvider> {

    override fun apply(context: CoreContext, props: Properties): RegexVariableProvider {
        val regexes = props.get<List<RegexVariable>>("regexes")
        val primary = props.getOrNull<String>("primary")
        return RegexVariableProvider(regexes, primary)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(ComponentType.variableProvider("regex"))
    }
}