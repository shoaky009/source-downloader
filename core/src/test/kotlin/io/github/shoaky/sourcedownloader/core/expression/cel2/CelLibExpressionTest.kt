package io.github.shoaky.sourcedownloader.core.expression.cel2

import io.github.shoaky.sourcedownloader.core.expression.CelCompiledExpressionFactory
import io.github.shoaky.sourcedownloader.core.expression.VariableType
import kotlin.test.Test
import kotlin.test.assertEquals

class CelLibExpressionTest {

    @Test
    fun contains_any_with_case_sensitive() {
        val expression = CelCompiledExpressionFactory.create(
            "item.tags.containsAny(['720P'])",
            Boolean::class.java,
            mapOf(
                "item" to VariableType.ANY
            )
        )
        val vars = mapOf("item" to mapOf("tags" to listOf("720p", "1080p")))
        assertEquals(false, expression.execute(vars))
    }

    @Test
    fun contains_any_with_case_not_sensitive() {
        val expression = CelCompiledExpressionFactory.create(
            "item.tags.containsAny(['720P'], true)",
            Boolean::class.java,
            mapOf(
                "item" to VariableType.ANY
            )
        )
        val vars = mapOf("item" to mapOf("tags" to listOf("720p", "1080p")))
        assertEquals(true, expression.execute(vars))
    }

    @Test
    fun strings_join_ignore_null() {
        val var1 = mapOf(
            "vars" to mapOf(
                "source" to null,
                "resolution" to "720P",
                "fps" to null
            )
        )

        val expression = CelCompiledExpressionFactory.create(
            "[vars.?test, vars.?fps, vars.?resolution].joinIgnoreNull('-', ' - ')",
            String::class.java,
            mapOf("vars" to VariableType.ANY)
        )
        assertEquals("720P", expression.execute(var1))

        val var2 = mapOf(
            "vars" to mapOf(
                "source" to null,
                "resolution" to "720P",
                "fps" to "60"
            )
        )
        assertEquals("60-720P", expression.execute(var2))
    }
}