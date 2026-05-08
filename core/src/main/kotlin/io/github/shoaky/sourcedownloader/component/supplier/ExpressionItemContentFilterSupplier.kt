package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.ExpressionItemContentFilter
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentMetadata
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.JsonSchema

object ExpressionItemContentFilterSupplier : ComponentSupplier<ExpressionItemContentFilter> {

    override fun apply(context: CoreContext, props: Properties): ExpressionItemContentFilter {
        val exclusions = props.getOrDefault<List<String>>("exclusions", listOf())
        val inclusions = props.getOrDefault<List<String>>("inclusions", listOf())
        return expressions(exclusions, inclusions)
    }

    fun expressions(
        exclusions: List<String> = emptyList(),
        inclusions: List<String> = emptyList()
    ): ExpressionItemContentFilter {
        return ExpressionItemContentFilter(exclusions, inclusions)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(ComponentType.itemContentFilter("expression"))
    }

    override fun metadata(): ComponentMetadata {
        return ComponentMetadata(
            propertySchema = JsonSchema(
                type = "object",
                properties = mapOf(
                    "exclusions" to JsonSchema(
                        type = "array",
                        items = JsonSchema(type = "string"),
                        default = emptyList<String>(),
                    ),
                    "inclusions" to JsonSchema(
                        type = "array",
                        items = JsonSchema(type = "string"),
                        default = emptyList<String>(),
                    )
                )
            )
        )
    }

}
