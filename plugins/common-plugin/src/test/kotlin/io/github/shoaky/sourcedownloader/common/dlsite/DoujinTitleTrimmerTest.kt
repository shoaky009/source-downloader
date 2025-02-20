package io.github.shoaky.sourcedownloader.common.dlsite

import org.junit.jupiter.api.Test

class DoujinTitleTrimmerTest {

    @Test
    fun test() {
        val testData = mapOf(
            "【1dsam】8888【222】" to "8888【222】",
            "【xa,ds.a】999999999【222】" to "999999999",
            "(1)8888【222】" to "(1)8888",
            "1234567890。1113" to "1234567890"
        )
        for ((value, expect) in testData.entries) {
            val result = DoujinTitleTrimmer.trim(value, 10)
            assert(result == expect) {
                "Expected: $expect, but got: $result"
            }
        }
    }
}