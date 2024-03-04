package io.github.shoaky.sourcedownloader.core

import io.github.shoaky.sourcedownloader.component.replacer.RegexVariableReplacer
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class RegexVariableReplacerSupplierTest {

    @Test
    fun test() {
        val replacer = RegexVariableReplacer("(?i)^BDRIP\$".toRegex(), "BD")
        val replace = replacer.replace("source", "BDrip")
        assertEquals("BD", replace)
    }

}