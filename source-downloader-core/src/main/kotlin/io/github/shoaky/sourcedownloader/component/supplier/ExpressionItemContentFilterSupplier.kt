package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.ExpressionItemContentFileter
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object ExpressionItemContentFilterSupplier : ComponentSupplier<ExpressionItemContentFileter> {

    override fun apply(props: Properties): ExpressionItemContentFileter {
        val exclusions = props.getOrDefault<List<String>>("exclusions", listOf())
        val inclusions = props.getOrDefault<List<String>>("inclusions", listOf())
        return expressions(exclusions, inclusions)
    }

    fun expressions(exclusions: List<String> = emptyList(), inclusions: List<String> = emptyList()): ExpressionItemContentFileter {
        return ExpressionItemContentFileter(exclusions, inclusions)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(ComponentType.contentFilter("expression"))
    }

}