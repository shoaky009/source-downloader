package xyz.shoaky.sourcedownloader.core

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.jayway.jsonpath.JsonPath
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.sdk.util.Jackson
import java.time.LocalDate

class PersistentItemPointerTest {

    @Test
    fun test_serialization() {
        val pointer = PersistentItemPointer(
            mutableMapOf(
                "offset" to 111,
                "ddd" to "333"
            )
        )
        val jsonPath = JsonPath.parse(Jackson.toJsonString(pointer))
        assertEquals(111, jsonPath.read("$.offset"))
        assertEquals("333", jsonPath.read("$.ddd"))
    }

    @Test
    fun test_deserialization() {
        val pointer = Jackson.fromJson<PersistentItemPointer>("""{"offset":111,"ddd":"333"}""", jacksonTypeRef())
        assertEquals(111, pointer.values["offset"])
        assertEquals("333", pointer.values["ddd"])
    }

    @Test
    fun convert() {
        val p = TestPointer1(
            LocalDate.now(),
            "1"
        )
        val convert = Jackson.convert(p, PersistentItemPointer::class)
        assertEquals(2, convert.values.size)
        assertEquals("1", convert.values["id"])
    }
}