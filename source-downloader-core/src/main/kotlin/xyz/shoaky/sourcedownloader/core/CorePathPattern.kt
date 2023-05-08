package xyz.shoaky.sourcedownloader.core

import com.fasterxml.jackson.annotation.JsonValue
import org.projectnessie.cel.checker.Decls
import org.projectnessie.cel.tools.ScriptHost
import xyz.shoaky.sourcedownloader.SourceDownloaderApplication.Companion.log
import xyz.shoaky.sourcedownloader.sdk.PathPattern
import xyz.shoaky.sourcedownloader.sdk.PathPattern.ExpressionResult
import xyz.shoaky.sourcedownloader.sdk.PathPattern.ParseResult
import xyz.shoaky.sourcedownloader.sdk.PatternVariables
import java.util.regex.Pattern


data class CorePathPattern(
    @get:JsonValue
    override val pattern: String
) : PathPattern {

    override fun parse(provider: PatternVariables): ParseResult {
        val matcher = variablePatternRegex.matcher(pattern)
        val result = StringBuilder()
        val variables = provider.variables()
        val varRes = mutableListOf<ExpressionResult>()
        while (matcher.find()) {
            val expression = Expression(matcher.group())
            val value = expression.eval(variables)

            varRes.add(ExpressionResult(expression.raw, value != null || expression.isOptional()))
            if (value != null) {
                matcher.appendReplacement(result, value)
            } else if (expression.isOptional()) {
                matcher.appendReplacement(result, "")
            }
        }
        matcher.appendTail(result)
        return ParseResult(result.toString(), varRes)
    }

    companion object {
        val origin = CorePathPattern("")

        private val variablePatternRegex: Pattern = Pattern.compile("\\{(.+?)}|:\\{(.+?)}")

    }
}

private class Expression(
    val raw: String
) {
    fun eval(variables: Map<String, String>): String? {
        val expression = parseRaw()
        return runCatching {
            val script = scriptHost.buildScript(expression)
                .withDeclarations(
                    *variables.map { Decls.newVar(it.key, Decls.String) }.toTypedArray()
                )
                .build()
            script.execute(String::class.java, variables)
        }.getOrElse {
            log.debug("eval expression '$expression' failed:{}", it.message)
            null
        }
    }

    private fun parseRaw(): String {
        if (raw.startsWith(":")) {
            return raw.substring(2, raw.length - 1)
        }
        return raw.substring(1, raw.length - 1)
    }

    fun isOptional(): Boolean {
        return raw.startsWith(":")
    }

    companion object {
        private val scriptHost = ScriptHost.newBuilder().build()
    }
}