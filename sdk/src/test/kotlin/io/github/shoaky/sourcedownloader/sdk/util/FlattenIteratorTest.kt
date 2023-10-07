package io.github.shoaky.sourcedownloader.sdk.util

import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class FlattenIteratorTest {

    @Test
    fun test() {
        val data = (0..20).toList()
        val flatten = data.chunked(2).flatten()
        assertContentEquals(data, flatten.toList())
    }

    @Test
    fun given_empty_list() {
        val data = listOf<Int>()
        val flatten = data.chunked(2).flatten()
        assertContentEquals(data, flatten.toList())
    }
}