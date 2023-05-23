package xyz.shoaky.sourcedownloader.core

import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.RegexVariableMatcher
import xyz.shoaky.sourcedownloader.sdk.MapPatternVariables
import kotlin.test.assertEquals

class CorePathPatternTest {
    @Test
    fun depth() {
        val pathPattern = CorePathPattern("{name}/{title}")
        assertEquals(2, pathPattern.depth())
    }

    @Test
    fun normal_parse() {
        val pathPattern = CorePathPattern("{name}/{title}abc")
        val variables = MapPatternVariables(mapOf(
            "name" to "111",
            "title" to "test"
        ))
        val parseResult = pathPattern.parse(variables)
        assertEquals("111/testabc", parseResult.path)
        assertEquals(true, parseResult.results.all { it.success })
    }

    @Test
    fun given_option_pattern_with_exists_variables() {
        val pathPattern = CorePathPattern("{name}/:{title}abc")
        val variables = MapPatternVariables(mapOf(
            "name" to "111",
            "title" to "test"
        ))
        val parseResult = pathPattern.parse(variables)
        assertEquals("111/testabc", parseResult.path)
        assertEquals(true, parseResult.results.all { it.success })
    }

    @Test
    fun given_option_pattern_with_not_exists_variables() {
        val pathPattern = CorePathPattern("{name}/:{title}abc")
        val variables = MapPatternVariables(mapOf(
            "name" to "111",
        ))
        val parseResult = pathPattern.parse(variables)
        assertEquals("111/abc", parseResult.path)
        assertEquals(true, parseResult.results.all { it.success })
    }


    @Test
    fun given_expression() {
        val pathPattern = CorePathPattern("{'test '+name} E{episode + '1'}:{' - '+source}")
        val variables = MapPatternVariables(mapOf(
            "name" to "111",
            "episode" to "2",
            "source" to "1"
        ))
        val parseResult = pathPattern.parse(variables)
        assertEquals("test 111 E21 - 1", parseResult.path)
        assertEquals(true, parseResult.results.all { it.success })

        val variables2 = MapPatternVariables(mapOf(
            "name" to "111",
            "episode" to "2",
        ))
        val result2 = pathPattern.parse(variables2)
        assertEquals("test 111 E21", result2.path)
        assertEquals(true, result2.results.all { it.success })
    }

    @Test
    fun test_replacement() {
        val pathPattern = CorePathPattern(
            "{title}-{source}",
            mapOf(
                RegexVariableMatcher("BDRIP".toRegex(RegexOption.IGNORE_CASE)) to "BD"
            )
        )

        val parse1 = pathPattern.parse(MapPatternVariables(mapOf(
            "title" to "111",
            "source" to "Web",
        )))
        assertEquals("111-Web", parse1.path)

        val parse = pathPattern.parse(MapPatternVariables(mapOf(
            "title" to "111",
            "source" to "BDrip",
        )))
        assertEquals("111-BD", parse.path)
    }
}