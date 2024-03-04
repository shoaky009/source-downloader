package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.replacer.RegexVariableReplacer
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object RegexVariableReplacerSupplier : ComponentSupplier<RegexVariableReplacer> {

    override fun apply(context: CoreContext, props: Properties): RegexVariableReplacer {
        return RegexVariableReplacer(props.get<Regex>("regex"), props.get("replacement"))
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.variableReplacer("regex")
        )
    }
}