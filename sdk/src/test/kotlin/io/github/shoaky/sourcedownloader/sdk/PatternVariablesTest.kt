package io.github.shoaky.sourcedownloader.sdk

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PatternVariablesTest {

    @Test
    fun given_null_value_should_empty() {
        val variables = TestData().variables()
        assertEquals(true, variables.isEmpty())
    }

}

private data class TestData(
    val name: String? = null,
) : PatternVariables