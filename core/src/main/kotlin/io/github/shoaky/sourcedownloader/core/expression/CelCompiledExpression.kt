package io.github.shoaky.sourcedownloader.core.expression

import org.projectnessie.cel.tools.Script

class CelCompiledExpression<T>(
    private val script: Script,
    private val resultType: Class<T>,
    private val rawString: String,
) : CompiledExpression<T> {

    var optional: Boolean = false

    override fun execute(variables: Map<String, Any>): T {
        return script.execute(resultType, variables)
    }

    override fun raw(): String {
        return rawString
    }

    override fun optional(): Boolean {
        return optional
    }

}