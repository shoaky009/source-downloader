package io.github.shoaky.sourcedownloader.core.file

import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.google.api.expr.v1alpha1.Expr
import io.github.shoaky.sourcedownloader.core.expression.*
import io.github.shoaky.sourcedownloader.sdk.PathPattern
import io.github.shoaky.sourcedownloader.util.jackson.PathPatternDeserializer
import org.projectnessie.cel.Env
import java.util.regex.Pattern

@JsonDeserialize(using = PathPatternDeserializer::class)
data class CorePathPattern(
    @get:JsonValue
    override val pattern: String,
    private val expressionFactory: CompiledExpressionFactory = CelCompiledExpressionFactory
) : PathPattern {

    val expressions: List<CompiledExpression<String>> by lazy {
        val matcher = variablePatternRegex.matcher(pattern)
        val expressions = mutableListOf<CompiledExpression<String>>()
        while (matcher.find()) {
            val raw = matcher.group()
            val parsed = parseRaw(raw)
            // FIXME 需要重新设计，这里不太合理
            val defs = if (expressionFactory is CelCompiledExpressionFactory) {
                defs(parsed)
            } else {
                emptyMap()
            }
            val expression = expressionFactory.create(parsed, String::class.java, defs)
            if (expression is CelCompiledExpression) {
                expression.optional = isOptional(raw)
            }
            expressions.add(expression)
        }
        expressions
    }

    private fun parseRaw(raw: String): String {
        if (raw.startsWith(OPTIONAL_EXPRESSION_PREFIX)) {
            return raw.substring(2, raw.length - 1)
        }
        return raw.substring(1, raw.length - 1)
    }

    private fun isOptional(raw: String): Boolean {
        return raw.startsWith(OPTIONAL_EXPRESSION_PREFIX)
    }

    private fun defs(parsed: String): Map<String, VariableType> {
        return buildMap {
            put("item", VariableType.ANY)
            put("file", VariableType.ANY)
            put("vars", VariableType.ANY)
            if (parsed.startsWith("item.") || parsed.startsWith("file.")) {
                return@buildMap
            }
            getDeclaredVariables(parsed).forEach {
                put(it, VariableType.STRING)
            }
        }
    }

    private fun getDeclaredVariables(parsed: String): Set<String> {
        val env = Env.newEnv()
        val parse = env.parse(parsed)
        return extractIdentifiers(parse.ast.expr)
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

    companion object {

        /**
         * {expression} or :{expression}
         */
        private val variablePatternRegex: Pattern = Pattern.compile("\\{(.+?)}|:\\{(.+?)}")
        val origin = CorePathPattern("")
        private const val OPTIONAL_EXPRESSION_PREFIX = ":"

    }

}