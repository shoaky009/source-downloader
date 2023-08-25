package io.github.shoaky.sourcedownloader.core.processor

import io.github.shoaky.sourcedownloader.sdk.MapPatternVariables
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class NameReplacePatternVariablesTest {

    @Test
    fun test() {
        val patternVariables = NameReplacePatternVariables(
            MapPatternVariables(
                mapOf(
                    "episodeNumber" to "01",
                    "season" to "02"
                )
            ),
            mapOf(
                "episodeNumber" to "episode",
                "test1" to "test"
            )
        )

        val variables = patternVariables.variables()
        assertEquals("01", variables["episode"])
        assertEquals("02", variables["season"])
        assert(variables.containsKey("episodeNumber").not())
        assert(variables.containsKey("test1").not())
        assert(variables.containsKey("test").not())
    }
}