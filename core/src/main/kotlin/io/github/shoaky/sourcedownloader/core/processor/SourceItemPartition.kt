package io.github.shoaky.sourcedownloader.core.processor

import io.github.shoaky.sourcedownloader.core.expression.CompiledExpression
import io.github.shoaky.sourcedownloader.core.expression.variables
import io.github.shoaky.sourcedownloader.sdk.SourceItem

interface SourceItemPartition {

    fun match(item: SourceItem): Boolean
}

class ExpressionSourceItemPartition(
    private val expression: CompiledExpression<Boolean>
) : SourceItemPartition {

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