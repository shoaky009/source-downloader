package io.github.shoaky.sourcedownloader.core.processor

import io.github.shoaky.sourcedownloader.core.expression.CompiledExpression
import io.github.shoaky.sourcedownloader.core.expression.variables
import io.github.shoaky.sourcedownloader.sdk.SourceFile

interface SourceFilePartition {

    fun match(sourceFile: SourceFile, fileCount: Int): Boolean

}

class ExpressionSourceFilePartition(
    private val expression: CompiledExpression<Boolean>
) : SourceFilePartition {

    override fun match(sourceFile: SourceFile, fileCount: Int): Boolean {
        val variables = sourceFile.variables(fileCount)
        return expression.execute(variables)
    }
}

class TagSourceFilePartition(
    private val tags: Set<String>
) : SourceFilePartition {

    override fun match(sourceFile: SourceFile, fileCount: Int): Boolean {
        return sourceFile.tags.containsAll(tags)
    }
}