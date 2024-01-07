package io.github.shoaky.sourcedownloader.core.expression

interface CompiledExpression<T> {

    fun execute(variables: Map<String, Any>): T

    fun executeIgnoreError(variables: Map<String, Any>): T?

    fun raw(): String

    fun optional(): Boolean = false

}