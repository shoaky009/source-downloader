package io.github.shoaky.sourcedownloader.core.expression.cel2

import dev.cel.runtime.CelRuntime
import dev.cel.runtime.CelUnknownSet
import io.github.shoaky.sourcedownloader.core.expression.CompiledExpression
import io.github.shoaky.sourcedownloader.sdk.util.Jackson

class Cel2CompiledExpression<T>(
    private val program: CelRuntime.Program,
    private val resultType: Class<T>,
    private val rawString: String,
) : CompiledExpression<T> {

    var optional: Boolean = false

    override fun execute(variables: Map<String, Any>): T {
        val result = program.eval(variables)
        if (result is CelUnknownSet) {
            throw IllegalStateException("Failed to execute expression: $rawString, unknown set:$result")
        }
        return Jackson.convert(result, resultType)
    }

    override fun raw(): String {
        return rawString
    }

    override fun optional(): Boolean {
        return optional
    }

}