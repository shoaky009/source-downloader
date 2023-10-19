package io.github.shoaky.sourcedownloader.common

import io.github.shoaky.sourcedownloader.sourceItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class KeywordVariableProviderTest {

    @Test
    fun given_prefixes_and_suffixes() {
        val provider = KeywordVariableProvider(
            listOf("1111")
        )

        val group1 = provider.createSourceGroup(sourceItem("(1111)zxcvbnm"))
        assertEquals(group1.sharedPatternVariables().variables()["keyword"], "1111")

        val group2 = provider.createSourceGroup(sourceItem("[1111]zxcvbnm"))
        assertEquals(group2.sharedPatternVariables().variables()["keyword"], "1111")
    }

    @Test
    fun given_no_prefixes_and_suffixes() {
        val provider = KeywordVariableProvider(
            listOf("1111")
        )

        val group2 = provider.createSourceGroup(sourceItem("1111zxcvbnm"))
        assert(group2.sharedPatternVariables().variables().contains("1111").not())
    }

    @Test
    fun given_keyword_and_mode() {
        val provider = KeywordVariableProvider(
            listOf("2222|1")
        )
        val group2 = provider.createSourceGroup(sourceItem("2222zxcvbnm"))
        assertEquals(group2.sharedPatternVariables().variables()["keyword"], "2222")
    }
}