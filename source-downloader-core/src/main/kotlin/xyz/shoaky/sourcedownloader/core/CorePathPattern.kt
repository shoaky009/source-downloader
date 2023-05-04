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
            val variableName = matcher.group(1)
            val variableValue = variables[variableName]
            varRes.add(VariableResult(variableName, variableValue != null))
            if (variableValue != null) {
                matcher.appendReplacement(result, variableValue)
            }
        }
        matcher.appendTail(result)
        return ParseResult(result.toString(), varRes)
    }

    companion object {
        @JvmStatic
        val VARIABLE_PATTERN: Pattern = Pattern.compile("\\{(.+?)}")

        @JvmStatic
        val ORIGIN = CorePathPattern("")
    }
}