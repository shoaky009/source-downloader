package xyz.shoaky.sourcedownloader.core.file

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.sdk.MapPatternVariables

class SharedPatternVariablesTest {

    @Test
    fun test() {
        val shared = SharedPatternVariables(
            MapPatternVariables(mapOf("season" to "01"))
        )
        shared.addShared(MapPatternVariables(mapOf("season" to "02")))
        assertEquals("01", shared.variables()["season"])
    }
}