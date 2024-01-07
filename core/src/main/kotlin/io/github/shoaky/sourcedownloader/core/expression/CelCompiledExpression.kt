package io.github.shoaky.sourcedownloader.core.expression

import org.projectnessie.cel.tools.Script
import org.slf4j.LoggerFactory

class CelCompiledExpression<T>(
    private val script: Script,
    private val resultType: Class<T>
) : CompiledExpression<T> {

    var optional: Boolean = false

    override fun execute(variables: Map<String, Any>): T {
        return script.execute(resultType, variables)
    }

    override fun executeIgnoreError(variables: Map<String, Any>): T? {
        return runCatching {
            script.execute(resultType, variables)
        }.getOrElse {
            log.debug("Execute expression '${raw()}' failed:{}", it.message)
            null
        }
    }

    override fun raw(): String {
        return script.toString()
    }

    override fun optional(): Boolean {
        return optional
    }

    companion object {

        private val log = LoggerFactory.getLogger(CelCompiledExpression::class.java)
    }

}