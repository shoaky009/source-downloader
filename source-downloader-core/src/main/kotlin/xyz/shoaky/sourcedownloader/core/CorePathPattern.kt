package xyz.shoaky.sourcedownloader.core

import com.fasterxml.jackson.annotation.JsonValue
import xyz.shoaky.sourcedownloader.sdk.PathPattern
import xyz.shoaky.sourcedownloader.sdk.PathPattern.ParseResult
import xyz.shoaky.sourcedownloader.sdk.PathPattern.VariableResult
import xyz.shoaky.sourcedownloader.sdk.PatternVariables
import java.util.regex.Pattern

data class CorePathPattern(
    @get:JsonValue
    override val pattern: String
) : PathPattern {

    override fun parse(provider: PatternVariables): ParseResult {
        val matcher = VARIABLE_PATTERN.matcher(pattern)
        val result = StringBuilder()
        val variables = provider.variables()
        val varRes = mutableListOf<VariableResult>()
        while (matcher.find()) {
            val expression = Expression(matcher.group())
            // TODO 表达式支持
            val expressionString = expression.getExpression()
            val variableValue = variables[expressionString]

            varRes.add(VariableResult(expressionString, variableValue != null || expression.isOptional()))
            if (variableValue != null) {
                matcher.appendReplacement(result, variableValue)
            } else if (expression.isOptional()) {
                matcher.appendReplacement(result, "")
            }
        }
        matcher.appendTail(result)
        return ParseResult(result.toString(), varRes)
    }

    companion object {
        @JvmStatic
        val VARIABLE_PATTERN: Pattern = Pattern.compile("\\{(.+?)}|:\\{(.+?)}")

        @JvmStatic
        val ORIGIN = CorePathPattern("")
    }
}

private class Expression(
    val raw: String
) {
    fun getExpression(): String {
        if (raw.startsWith(":")) {
            return raw.substring(2, raw.length - 1)
        }
        return raw.substring(1, raw.length - 1)
    }

    fun isOptional(): Boolean {
        return raw.startsWith(":")
    }
}