package io.github.shoaky.sourcedownloader.core

import io.github.shoaky.sourcedownloader.core.file.CorePathPattern
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CorePathPatternTest {

    @Test
    fun depth() {
        val pathPattern = CorePathPattern("{name}/{title}")
        assertEquals(2, pathPattern.depth())
    }

}