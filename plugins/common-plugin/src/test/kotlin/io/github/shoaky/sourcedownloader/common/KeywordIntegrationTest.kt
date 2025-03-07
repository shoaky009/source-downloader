package io.github.shoaky.sourcedownloader.common

import io.github.shoaky.sourcedownloader.sourceItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class KeywordIntegrationTest {

    @Test
    fun given_prefixes_and_suffixes() {
        val provider = KeywordIntegration(
            listOf("1111")
        )

        val group1 = provider.itemVariables(sourceItem("(1111)zxcvbnm"))
        assertEquals(group1.variables()["keyword"], "1111")

        val group2 = provider.itemVariables(sourceItem("[1111]zxcvbnm"))
        assertEquals(group2.variables()["keyword"], "1111")
    }

    @Test
    fun given_no_prefixes_and_suffixes() {
        val provider = KeywordIntegration(
            listOf("1111")
        )

        val group2 = provider.itemVariables(sourceItem("1111zxcvbnm"))
        assert(group2.variables().contains("1111").not())
    }

    @Test
    fun given_keyword_and_mode() {
        val provider = KeywordIntegration(
            listOf("2222|1")
        )
        val group2 = provider.itemVariables(sourceItem("2222zxcvbnm"))
        assertEquals(group2.variables()["keyword"], "2222")
    }

    @Test
    fun given_alias_keyword_and_mode() {
        val provider = KeywordIntegration(
            listOf("2222|1|abc222")
        )
        val group2 = provider.itemVariables(sourceItem("2222zxcvbnm"))
        assertEquals(group2.variables()["keyword"], "abc222")
    }
}