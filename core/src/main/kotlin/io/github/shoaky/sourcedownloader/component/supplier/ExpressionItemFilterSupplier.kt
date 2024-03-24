package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.ExpressionItemFilter
import io.github.shoaky.sourcedownloader.core.expression.CompiledExpressionFactory
import io.github.shoaky.sourcedownloader.core.expression.ExpressionType
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

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

}