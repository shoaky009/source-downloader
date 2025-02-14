package io.github.shoaky.sourcedownloader.component

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ForceTrimmerTest {

    @Test
    fun test() {
        assertEquals("te", ForceTrimmer.trim("test", 2))
    }
}