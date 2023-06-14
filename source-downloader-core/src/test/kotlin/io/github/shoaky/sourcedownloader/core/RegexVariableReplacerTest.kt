package io.github.shoaky.sourcedownloader.core

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class RegexVariableReplacerTest {

    @Test
    fun test() {
        val replacer = RegexVariableReplacer("(?i)^BDRIP\$".toRegex(), "BD")
        val replace = replacer.replace("source", "BDrip")
        assertEquals("BD", replace)
    }

}