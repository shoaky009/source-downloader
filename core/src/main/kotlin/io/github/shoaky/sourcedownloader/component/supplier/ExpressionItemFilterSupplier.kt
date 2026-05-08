package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.ExpressionItemFilter
import io.github.shoaky.sourcedownloader.core.expression.CompiledExpressionFactory
import io.github.shoaky.sourcedownloader.core.expression.ExpressionType
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentMetadata
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.JsonSchema

object ExpressionItemFilterSupplier : ComponentSupplier<ExpressionItemFilter> {

    override fun apply(context: CoreContext, props: Properties): ExpressionItemFilter {
        val exclusions = props.getOrDefault<List<String>>("exclusions", listOf())
        val inclusions = props.getOrDefault<List<String>>("inclusions", listOf())
        val type = props.getOrDefault<ExpressionType>("type", ExpressionType.CEL)
        return expressions(exclusions, inclusions, type.factory)
    }

    fun expressions(
        exclusions: List<String> = emptyList(),
        inclusions: List<String> = emptyList(),
        factory: CompiledExpressionFactory
    ): ExpressionItemFilter {
        return ExpressionItemFilter(exclusions, inclusions, factory)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(ComponentType.itemFilter("expression"))
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
                    ),
                    "type" to JsonSchema(
                        type = "string",
                        enum = listOf("CEL"),
                        default = "CEL"
                    )
                )
            )
        )
    }

}
