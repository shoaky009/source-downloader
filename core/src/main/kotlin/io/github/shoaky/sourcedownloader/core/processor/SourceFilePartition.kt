package io.github.shoaky.sourcedownloader.core.processor

import io.github.shoaky.sourcedownloader.core.expression.CompiledExpression
import io.github.shoaky.sourcedownloader.core.expression.variables
import io.github.shoaky.sourcedownloader.sdk.SourceFile

interface SourceFilePartition {

    fun match(sourceFile: SourceFile): Boolean

}

class ExpressionSourceFilePartition(
    private val expression: CompiledExpression<Boolean>
) : SourceFilePartition {

    override fun match(sourceFile: SourceFile): Boolean {
        return expression.execute(sourceFile.variables())
    }
}

class TagSourceFilePartition(
    private val tags: Set<String>
) : SourceFilePartition {

    override fun match(sourceFile: SourceFile): Boolean {
        return sourceFile.tags.containsAll(tags)
    }
}