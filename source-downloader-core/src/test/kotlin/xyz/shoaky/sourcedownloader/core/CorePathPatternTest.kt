package xyz.shoaky.sourcedownloader.core

import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.sdk.MapPatternVariables

class CorePathPatternTest {
    @Test
    fun depth() {
        val pathPattern = CorePathPattern("{name}/{title}")
        kotlin.test.assertEquals(2, pathPattern.depth())
    }

    @Test
    fun normal_parse() {
        val pathPattern = CorePathPattern("{name}/{title}abc")
        val variables = MapPatternVariables(mapOf(
            "name" to "111",
            "title" to "test"
        ))
        val parseResult = pathPattern.parse(variables)
        kotlin.test.assertEquals("111/testabc", parseResult.path)
        kotlin.test.assertEquals(true, parseResult.results.all { it.success })
    }

    @Test
    fun given_option_pattern_with_exists_variables() {
        val pathPattern = CorePathPattern("{name}/:{title}abc")
        val variables = MapPatternVariables(mapOf(
            "name" to "111",
            "title" to "test"
        ))
        val parseResult = pathPattern.parse(variables)
        kotlin.test.assertEquals("111/testabc", parseResult.path)
        kotlin.test.assertEquals(true, parseResult.results.all { it.success })
    }

    @Test
    fun given_option_pattern_with_not_exists_variables() {
        val pathPattern = CorePathPattern("{name}/:{title}abc")
        val variables = MapPatternVariables(mapOf(
            "name" to "111",
        ))
        val parseResult = pathPattern.parse(variables)
        kotlin.test.assertEquals("111/abc", parseResult.path)
        kotlin.test.assertEquals(true, parseResult.results.all { it.success })
    }
}