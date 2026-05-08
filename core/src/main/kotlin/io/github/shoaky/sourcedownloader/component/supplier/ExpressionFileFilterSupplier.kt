package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.ExpressionFileFilter
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentMetadata
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.JsonSchema

object ExpressionFileFilterSupplier : ComponentSupplier<ExpressionFileFilter> {

    override fun apply(context: CoreContext, props: Properties): ExpressionFileFilter {
        val exclusions = props.getOrDefault<List<String>>("exclusions", listOf())
        val inclusions = props.getOrDefault<List<String>>("inclusions", listOf())
        return ExpressionFileFilter(exclusions, inclusions)
    }

    fun expressions(
        exclusions: List<String> = emptyList(),
        inclusions: List<String> = emptyList()
    ): ExpressionFileFilter {
        return ExpressionFileFilter(
            exclusions, inclusions
        )
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.fileContentFilter("expression")
        )
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
