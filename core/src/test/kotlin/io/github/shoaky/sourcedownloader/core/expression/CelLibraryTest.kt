package io.github.shoaky.sourcedownloader.core.expression

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CelLibraryTest {

    @Test
    fun given_containsAny_when_listContainsAny_then_returnTrue() {
        val res = CelLibrary.containsAny(
            listOf("720p"), listOf("720P"), true
        )
        assertEquals(true, res)
    }
}