package io.github.shoaky.sourcedownloader.sdk

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertNull

class PropertiesTest {

    private val props = Properties.fromMap(
        mapOf(
            "test" to "1",
            "list" to listOf(1, 2)
        )
    )

    @Test
    fun normal() {
        assertEquals(1, props.get<Int>("test"))
        assertContentEquals(listOf("1", "2"), props.get<List<String>>("list"))
    }

    @Test
    fun test_raw() {
        assertEquals("1", props.getRaw("test"))
    }

    @Test
    fun get_or_null() {
        assertEquals(1, props.getOrNull<Int>("test"))
        assertNull(props.getOrNull<Int>("test1"))
    }

    @Test
    fun test_default() {
        assertEquals(1, props.getOrDefault("test2", 1))
        assertEquals(1, props.getOrDefault("test", 3))
    }
}