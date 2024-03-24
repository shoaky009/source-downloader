package io.github.shoaky.sourcedownloader.core.expression

import org.springframework.context.expression.MapAccessor
import org.springframework.expression.Expression
import org.springframework.expression.spel.support.StandardEvaluationContext

class SpelCompiledExpression<T>(
    private val expression: Expression,
    private val resultType: Class<T>,
) : CompiledExpression<T> {

    override fun execute(variables: Map<String, Any>): T {
        return expression.getValue(context, variables, resultType) ?: throw NullPointerException()
    }

    override fun raw(): String {
        return expression.expressionString
    }

    companion object {

        private val context = StandardEvaluationContext()

        init {
            context.addPropertyAccessor(MapAccessor())
            buildInFunction()
        }

        private fun buildInFunction() {
            context.registerFunction(
                "containsAny",
                CelLibrary::class.java.getDeclaredMethod(
                    "containsAny",
                    Collection::class.java,
                    Collection::class.java,
                    Boolean::class.java
                )
            )
        }
    }
}