package io.github.shoaky.sourcedownloader.core.expression

import org.slf4j.LoggerFactory

interface CompiledExpression<T> {

    fun execute(variables: Map<String, Any>): T

    fun executeIgnoreError(variables: Map<String, Any>): T? {
        return runCatching {
            execute(variables)
        }.getOrElse {
            log.debug("Execute expression '${raw()}' failed:{}", it.message)
            null
        }
    }

    fun raw(): String

    fun optional(): Boolean = false

    companion object {

        private val log = LoggerFactory.getLogger(CompiledExpression::class.java)
    }

}