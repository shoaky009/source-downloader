package io.github.shoaky.sourcedownloader.core

import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.google.api.expr.v1alpha1.Expr
import io.github.shoaky.sourcedownloader.SourceDownloaderApplication.Companion.log
import io.github.shoaky.sourcedownloader.sdk.PathPattern
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
) : PathPattern {

    val expressions: List<Expression> by lazy {
        val matcher = variablePatternRegex.matcher(pattern)
        val expressions = mutableListOf<Expression>()
        while (matcher.find()) {
            expressions.add(Expression(matcher.group()))
        }
        expressions
    }

    companion object {

        /**
         * {expression} or :{expression}
         */
        private val variablePatternRegex: Pattern = Pattern.compile("\\{(.+?)}|:\\{(.+?)}")
        val ORIGIN = CorePathPattern("")

    }

}

class Expression(
    val raw: String
) {

    private val script: Script by lazy {
        val buildScript = scriptHost.buildScript(parseRaw())
        buildScript.withDeclarations(
            Decls.newVar("item.attrs", Decls.newMapType(Decls.String, Decls.String)),
            Decls.newVar("file.attrs", Decls.newMapType(Decls.String, Decls.String)),
            Decls.newVar("item.date", Decls.String),
            Decls.newVar("item.title", Decls.String),
            *this.getDeclaredVariables().map { Decls.newVar(it, Decls.String) }.toTypedArray(),
        )
        buildScript.build()
    }

    fun eval(variables: Map<String, Any>): String? {
        val expression = parseRaw()
        return runCatching {
            script.execute(String::class.java, variables)
        }.getOrElse {
            log.debug("eval expression '$expression' failed:{}", it.message)
            null
        }
    }

    private fun parseRaw(): String {
        if (raw.startsWith(OPTIONAL_EXPRESSION_PREFIX)) {
            return raw.substring(2, raw.length - 1)
        }
        return raw.substring(1, raw.length - 1)
    }

    private fun getDeclaredVariables(): Set<String> {
        val env = Env.newEnv()
        val parse = env.parse(parseRaw())
        return extractIdentifiers(parse.ast.expr)
    }

    fun isOptional(): Boolean {
        return raw.startsWith(OPTIONAL_EXPRESSION_PREFIX)
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

        private const val OPTIONAL_EXPRESSION_PREFIX = ":"
    }
}
