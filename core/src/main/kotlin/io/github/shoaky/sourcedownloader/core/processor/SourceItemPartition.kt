package io.github.shoaky.sourcedownloader.core.processor

import io.github.shoaky.sourcedownloader.core.expression.CompiledExpression
import io.github.shoaky.sourcedownloader.core.expression.CompiledExpressionFactory
import io.github.shoaky.sourcedownloader.core.expression.sourceItemDefs
import io.github.shoaky.sourcedownloader.core.expression.variables
import io.github.shoaky.sourcedownloader.sdk.SourceItem

interface SourceItemPartition {

    fun match(item: SourceItem): Boolean
}

class ExpressionSourceItemPartition(
    expression: String,
    expressionFactory: CompiledExpressionFactory,
) : SourceItemPartition {

    private val expression: CompiledExpression<Boolean> by lazy {
        expressionFactory.create(expression, Boolean::class.java, sourceItemDefs())
    }

    override fun match(item: SourceItem): Boolean {
        return expression.execute(item.variables())
    }
}

class TagSourceItemPartition(
    private val tags: Set<String>
) : SourceItemPartition {

    override fun match(item: SourceItem): Boolean {
        return item.tags.containsAll(tags)
    }
}