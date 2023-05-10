package xyz.shoaky.sourcedownloader.core

import com.fasterxml.jackson.annotation.JsonValue
import com.google.api.expr.v1alpha1.Expr
import org.projectnessie.cel.Env
import org.projectnessie.cel.checker.Decls
import org.projectnessie.cel.tools.Script
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

    private val expressions: List<Expression> by lazy {
        val matcher = variablePatternRegex.matcher(pattern)
        val expressions = mutableListOf<Expression>()
        while (matcher.find()) {
            expressions.add(Expression(matcher.group()))
        }
        expressions
    }

    override fun parse(provider: PatternVariables): ParseResult {
        val matcher = variablePatternRegex.matcher(pattern)
        val pathBuilder = StringBuilder()
        val variables = provider.variables()
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

    companion object {
        private val variablePatternRegex: Pattern = Pattern.compile("\\{(.+?)}|:\\{(.+?)}")
        val origin = CorePathPattern("")

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

    companion object {
        private val scriptHost = ScriptHost.newBuilder().build()
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