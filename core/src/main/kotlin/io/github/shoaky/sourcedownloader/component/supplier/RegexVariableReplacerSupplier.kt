package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.replacer.RegexVariableReplacer
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentMetadata
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.JsonSchema

object RegexVariableReplacerSupplier : ComponentSupplier<RegexVariableReplacer> {

    override fun apply(context: CoreContext, props: Properties): RegexVariableReplacer {
        return RegexVariableReplacer(props.get<Regex>("regex"), props.get("replacement"))
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.variableReplacer("regex")
        )
    }

    override fun metadata(): ComponentMetadata {
        return ComponentMetadata(
            propertySchema = JsonSchema(
                type = "object",
                required = listOf("regex", "replacement"),
                properties = mapOf(
                    "regex" to JsonSchema(type = "string"),
                    "replacement" to JsonSchema(type = "string")
                )
            )
        )
    }
}
