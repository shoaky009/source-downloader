package xyz.shoaky.sourcedownloader.component.supplier

import xyz.shoaky.sourcedownloader.component.ExpressionItemFilter
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier
import xyz.shoaky.sourcedownloader.sdk.component.SourceItemFilter
import xyz.shoaky.sourcedownloader.util.SpringExpression

object ExpressionItemFilterSupplier : SdComponentSupplier<ExpressionItemFilter> {

    override fun apply(props: ComponentProps): ExpressionItemFilter {
        val exclusions = props.getOrDefault<List<String>>("exclusions", listOf())
        val inclusions = props.getOrDefault<List<String>>("inclusions", listOf())
        return expressions(exclusions, inclusions)
    }

    fun expressions(exclusions: List<String> = emptyList(), inclusions: List<String> = emptyList()): ExpressionItemFilter {
        return ExpressionItemFilter(
            exclusions.map { SpringExpression.parseExpression(it) },
            inclusions.map { SpringExpression.parseExpression(it) }
        )
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(ComponentType("expression", SourceItemFilter::class))
    }

}