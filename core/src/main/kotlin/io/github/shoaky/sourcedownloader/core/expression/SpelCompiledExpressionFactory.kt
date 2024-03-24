package io.github.shoaky.sourcedownloader.core.expression

import org.springframework.expression.spel.SpelCompilerMode
import org.springframework.expression.spel.SpelParserConfiguration
import org.springframework.expression.spel.standard.SpelExpressionParser

object SpelCompiledExpressionFactory : CompiledExpressionFactory {

    private val parser = SpelExpressionParser(
        SpelParserConfiguration(SpelCompilerMode.IMMEDIATE, null)
    )

    override fun <T> create(raw: String, resultType: Class<T>, def: Map<String, VariableType>): CompiledExpression<T> {
        val spel = parser.parseRaw(raw)
        return SpelCompiledExpression(spel, resultType)
    }
}