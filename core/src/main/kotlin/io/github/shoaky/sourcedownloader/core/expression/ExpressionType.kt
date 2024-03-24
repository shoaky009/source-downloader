package io.github.shoaky.sourcedownloader.core.expression

enum class ExpressionType(val factory: CompiledExpressionFactory) {
    CEL(CelCompiledExpressionFactory),
    SPEL(SpelCompiledExpressionFactory)
}