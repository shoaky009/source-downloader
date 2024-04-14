package io.github.shoaky.sourcedownloader.util

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class JsonComparatorTest {

    @Test
    fun given_object() {
        val json11 = """
            {
                "a": 1,
                "b": 2,
                "c": {
                    "e": 4
                }
            }
        """.trimIndent()
        val json12 = """
            {
                "a": 1,
                "b": 3,
                "c": {
                    "d": 3,
                    "e": 5
                }
            }
        """.trimIndent()
        val diff1 = JsonComparator.findDifference(json11, json12)
        assertEquals("""{"b":3,"c":{"d":3,"e":5}}""", diff1.toString())
    }

    @Test
    fun given_objects_find_diff_value() {
        val json21 = """[{"test":111},{"test33":222}]"""
        val json22 = """[{"test":222},{"test33":222}]"""
        val diff2 = JsonComparator.findDifference(json21, json22)
        assertEquals("""[{"test":222}]""", diff2.toString())


    }

    @Test
    fun given_new_object_and_diff_value() {
        val json31 = """[{"test":111}]"""
        val json32 = """[{"test":222},{"test33":222}]"""
        val diff3 = JsonComparator.findDifference(json31, json32)
        assertEquals("""[{"test":222},{"test33":222}]""", diff3.toString())
    }

    @Test
    fun given_mixed() {
        val json1 = """
            {
                "a": 1,
                "b": 2,
                "c": {
                    "e": 4,
                    "array": [1, 2, 3],
                    "newObject": [{"test":111}],
                    "diffValue": [{"test":111}]
                }
            }
        """.trimIndent()
        val json2 = """
            {
                "a": 1,
                "b": 3,
                "c": {
                    "d": 3,
                    "e": 5,
                    "array": [4],
                    "newObject": [{"test":222},{"test33":222}],
                    "diffValue": [{"test":333}]
                }
            }
        """.trimIndent()
        val diff = JsonComparator.findDifference(json1, json2)
        assertEquals(
            """{"b":3,"c":{"d":3,"e":5,"array":[4],"newObject":[{"test":222},{"test33":222}],"diffValue":[{"test":333}]}}""",
            diff.toString()
        )
    }

}