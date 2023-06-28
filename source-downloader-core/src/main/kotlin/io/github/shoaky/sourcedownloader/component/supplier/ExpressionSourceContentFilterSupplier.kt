package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.ExpressionSourceContentFileter
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

object ExpressionSourceContentFilterSupplier : SdComponentSupplier<ExpressionSourceContentFileter> {

    override fun apply(props: Properties): ExpressionSourceContentFileter {
        val exclusions = props.getOrDefault<List<String>>("exclusions", listOf())
        val inclusions = props.getOrDefault<List<String>>("inclusions", listOf())
        return expressions(exclusions, inclusions)
    }

    fun expressions(exclusions: List<String> = emptyList(), inclusions: List<String> = emptyList()): ExpressionSourceContentFileter {
        return ExpressionSourceContentFileter(exclusions, inclusions)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(ComponentType.contentFilter("expression"))
    }

}