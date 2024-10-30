package io.github.shoaky.sourcedownloader.core.processor

import io.github.shoaky.sourcedownloader.core.expression.CompiledExpression
import io.github.shoaky.sourcedownloader.core.expression.CompiledExpressionFactory
import io.github.shoaky.sourcedownloader.core.expression.sourceFileDefs
import io.github.shoaky.sourcedownloader.core.expression.variables
import io.github.shoaky.sourcedownloader.sdk.SourceFile

interface SourceFilePartition {

    fun match(sourceFile: SourceFile): Boolean

}

class ExpressionSourceFilePartition(
    expression:String,
    expressionFactory: CompiledExpressionFactory,
) : SourceFilePartition {
    private val expression: CompiledExpression<Boolean> by lazy {
        expressionFactory.create(
            expression,
            Boolean::class.java,
            sourceFileDefs()
        )
    }

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