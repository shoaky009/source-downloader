package xyz.shoaky.sourcedownloader.component.supplier

import xyz.shoaky.sourcedownloader.component.ExpressionItemFilter
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier
import xyz.shoaky.sourcedownloader.sdk.component.SourceItemFilter

object ExpressionItemFilterSupplier : SdComponentSupplier<ExpressionItemFilter> {

    override fun apply(props: Properties): ExpressionItemFilter {
        val exclusions = props.getOrDefault<List<String>>("exclusions", listOf())
        val inclusions = props.getOrDefault<List<String>>("inclusions", listOf())
        return expressions(exclusions, inclusions)
    }

    fun expressions(exclusions: List<String> = emptyList(), inclusions: List<String> = emptyList()): ExpressionItemFilter {
        return ExpressionItemFilter(exclusions, inclusions)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(ComponentType("expression", SourceItemFilter::class))
    }

}