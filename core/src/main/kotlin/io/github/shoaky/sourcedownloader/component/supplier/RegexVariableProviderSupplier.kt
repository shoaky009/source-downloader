package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.provider.RegexVariable
import io.github.shoaky.sourcedownloader.component.provider.RegexVariableProvider
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentMetadata
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.JsonSchema

object RegexVariableProviderSupplier : ComponentSupplier<RegexVariableProvider> {

    override fun apply(context: CoreContext, props: Properties): RegexVariableProvider {
        val regexes = props.get<List<RegexVariable>>("regexes")
        val primary = props.getOrNull<String>("primary")
        return RegexVariableProvider(regexes, primary)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(ComponentType.variableProvider("regex"))
    }

    override fun metadata(): ComponentMetadata {
        return ComponentMetadata(
            propertySchema = JsonSchema(
                type = "object",
                required = listOf("regexes"),
                properties = mapOf(
                    "regexes" to JsonSchema(
                        type = "array",
                        items = JsonSchema(
                            type = "object",
                            required = listOf("name", "regex"),
                            properties = mapOf(
                                "name" to JsonSchema(type = "string"),
                                "regex" to JsonSchema(type = "string"),
                                "field" to JsonSchema(
                                    type = "string",
                                    enum = listOf("title", "link", "downloadUri", "contentType", "datetime"),
                                    default = "title"
                                )
                            )
                        )
                    ),
                    "primary" to JsonSchema(type = "string")
                )
            )
        )
    }
}
