package io.github.shoaky.sourcedownloader.telegram

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TelegramPointerTest {

    @Test
    fun test_update() {
        val pointer = TelegramPointer(
            mutableMapOf(1L to 0, -2L to 0)
        )

        pointer.update(ChatPointer(1, 100))
        assertEquals(100, pointer.getLastMessageId(1))
        assertEquals(0, pointer.getLastMessageId(-2))
    }

    @Test
    fun test_refresh() {
        val historyPointer = TelegramPointer(
            mutableMapOf(1L to 100,
                -2L to 0
            ))

        historyPointer.refreshChats(listOf(1, -3))
        assertEquals(2, historyPointer.chatLastMessageIds.size)
        assertEquals(100, historyPointer.getLastMessageId(1))
        assertEquals(0, historyPointer.getLastMessageId(-3))
    }
}