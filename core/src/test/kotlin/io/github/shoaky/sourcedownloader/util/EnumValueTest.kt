package io.github.shoaky.sourcedownloader.util

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class EnumValueTest {
    @Test
    fun test() {
        assertEquals(TestEnum.B, TestEnum::class.fromValue("1"))
    }
}

private enum class TestEnum : EnumValue<String> {

    A, B, C

    ;

    override fun getValue(): String {
        return this.ordinal.toString()
    }
}