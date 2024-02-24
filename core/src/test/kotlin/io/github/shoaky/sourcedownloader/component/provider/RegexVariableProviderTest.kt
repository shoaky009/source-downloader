package io.github.shoaky.sourcedownloader.component.provider

import io.github.shoaky.sourcedownloader.sourceItem
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class RegexVariableProviderTest {

    @Test
    fun normal() {
        val provider = RegexVariableProvider(
            listOf(RegexVariable("date", Regex("\\d+年\\d+月号.*")))
        )

        val group = provider.itemSharedVariables(sourceItem("dsadsa 2021年11月号 [IX]"))
        assertEquals("2021年11月号 [IX]", group.variables()["date"])
    }
}