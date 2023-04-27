package xyz.shoaky.sourcedownloader.component.supplier

import xyz.shoaky.sourcedownloader.component.ExpressionFileFilter
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.SdComponentSupplier
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.util.SpringExpression

object ExpressionFileFilterSupplier : SdComponentSupplier<ExpressionFileFilter> {
    override fun apply(props: Properties): ExpressionFileFilter {
        val exclusions = props.getOrDefault<List<String>>("exclusions", listOf())
        val inclusions = props.getOrDefault<List<String>>("inclusions", listOf())
        return expressions(exclusions, inclusions)
    }

    fun expressions(exclusions: List<String> = emptyList(), inclusions: List<String> = emptyList()): ExpressionFileFilter {
        return ExpressionFileFilter(
            exclusions.map { SpringExpression.parseExpression(it) },
            inclusions.map { SpringExpression.parseExpression(it) }
        )
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.fileFilter("expression")
        )
    }

}