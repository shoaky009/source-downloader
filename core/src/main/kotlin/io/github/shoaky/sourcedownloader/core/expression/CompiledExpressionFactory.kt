package io.github.shoaky.sourcedownloader.core.expression

interface CompiledExpressionFactory {

    fun <T> create(raw: String, resultType: Class<T>, def: Map<String, VariableType>): CompiledExpression<T>
}