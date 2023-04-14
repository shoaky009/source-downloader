package xyz.shoaky.sourcedownloader.sdk

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PathPatternTest {

    @Test
    fun depth() {
        val pathPattern = PathPattern("{name}/{title}")
        assertEquals(2, pathPattern.depth())
    }

    @Test
    fun normal_parse() {
        val pathPattern = PathPattern("{name}/{title}abc")
        val variables = MapPatternVariables(mapOf(
            "name" to "111",
            "title" to "test"
        ))
        val parseResult = pathPattern.parse(variables)
        assertEquals("111/testabc", parseResult.path)
        assertEquals(true, parseResult.results.all { it.success })
    }

}