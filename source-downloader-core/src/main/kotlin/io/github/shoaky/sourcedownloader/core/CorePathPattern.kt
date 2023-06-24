package io.github.shoaky.sourcedownloader.core

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.google.api.expr.v1alpha1.Expr
import io.github.shoaky.sourcedownloader.SourceDownloaderApplication.Companion.log
import io.github.shoaky.sourcedownloader.sdk.PathPattern
import io.github.shoaky.sourcedownloader.sdk.PathPattern.ExpressionResult
import io.github.shoaky.sourcedownloader.sdk.PathPattern.ParseResult
import io.github.shoaky.sourcedownloader.sdk.PatternVariables
import io.github.shoaky.sourcedownloader.util.jackson.PathPatternDeserializer
import io.github.shoaky.sourcedownloader.util.scriptHost
import org.projectnessie.cel.Env
import org.projectnessie.cel.checker.Decls
import org.projectnessie.cel.tools.Script
import java.util.regex.Pattern

@JsonDeserialize(using = PathPatternDeserializer::class)
data class CorePathPattern(
    @get:JsonValue
    override val pattern: String,
    /**
     * 不在[PatternVariables]装饰替换的原因是不想做成全局的，而是每个[PathPattern]有自己独立的替换规则
     */
    @JsonIgnore
    private var variableReplacers: MutableList<VariableReplacer> = mutableListOf()
) : PathPattern {

    private val expressions: List<Expression> by lazy {
        val matcher = variablePatternRegex.matcher(pattern)
        val expressions = mutableListOf<Expression>()
        while (matcher.find()) {
            expressions.add(Expression(matcher.group()))
        }
        expressions
    }

    fun addReplacer(vararg variableReplacers: VariableReplacer) {
        this.variableReplacers.addAll(variableReplacers)
    }

    override fun parse(provider: PatternVariables): ParseResult {
        val matcher = variablePatternRegex.matcher(pattern)
        val pathBuilder = StringBuilder()
        val variables = provider.variables().mapValues { entry ->
            var text = entry.value
            variableReplacers.forEach {
                val before = text
                text = it.replace(entry.key, text)
                if (before != text) {
                    log.debug("replace variable '{}' from '{}' to '{}'", entry.key, before, text)
                }
            }
            text
        }

        val variableResults = mutableListOf<ExpressionResult>()

        var expressionIndex = 0
        while (matcher.find()) {
            val expression = expressions[expressionIndex]
            val value = expression.eval(variables)
            variableResults.add(ExpressionResult(expression.raw, value != null || expression.isOptional()))
            if (value != null) {
                matcher.appendReplacement(pathBuilder, value)
            } else if (expression.isOptional()) {
                matcher.appendReplacement(pathBuilder, "")
            }
            expressionIndex = expressionIndex.inc()
        }
        matcher.appendTail(pathBuilder)
        return ParseResult(pathBuilder.toString(), variableResults)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CorePathPattern

        return pattern == other.pattern
    }

    override fun hashCode(): Int {
        return pattern.hashCode()
    }

    companion object {
        private val variablePatternRegex: Pattern = Pattern.compile("\\{(.+?)}|:\\{(.+?)}")
        val ORIGIN = CorePathPattern("")

    }

}

private class Expression(
    val raw: String
) {

    private val script: Script by lazy {
        val buildScript = scriptHost.buildScript(parseRaw())
        buildScript.withDeclarations(
            *this.getDeclaredVariables().map { Decls.newVar(it, Decls.String) }.toTypedArray()
        )
        buildScript
            .build()
    }

    fun eval(variables: Map<String, String>): String? {
        val expression = parseRaw()
        return runCatching {
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

    fun getDeclaredVariables(): Set<String> {
        val env = Env.newEnv()
        val parse = env.parse(parseRaw())
        return extractIdentifiers(parse.ast.expr)
    }

    fun isOptional(): Boolean {
        return raw.startsWith(":")
    }

    private fun extractIdentifiers(expr: Expr): Set<String> {
        val identifiers = mutableSetOf<String>()
        when (expr.exprKindCase) {
            Expr.ExprKindCase.IDENT_EXPR -> {
                val ident = expr.identExpr
                identifiers.add(ident.name)
            }

            Expr.ExprKindCase.SELECT_EXPR -> {
                val select = expr.selectExpr
                identifiers.addAll(extractIdentifiers(select.operand))
                identifiers.add(select.field)
            }

            Expr.ExprKindCase.CALL_EXPR -> {
                val call = expr.callExpr
                for (arg in call.argsList) {
                    identifiers.addAll(extractIdentifiers(arg))
                }
            }

            Expr.ExprKindCase.LIST_EXPR -> {
                val list = expr.listExpr
                for (elem in list.elementsList) {
                    identifiers.addAll(extractIdentifiers(elem))
                }
            }

            Expr.ExprKindCase.STRUCT_EXPR -> {
                val struct = expr.structExpr
                for (entry in struct.entriesList) {
                    identifiers.addAll(extractIdentifiers(entry.value))
                }
            }

            else -> {
                // do nothing
            }
        }
        return identifiers
    }
}
