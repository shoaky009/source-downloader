package io.github.shoaky.sourcedownloader.telegram

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TelegramPointerTest {

    @Test
    fun test_update() {
        val pointer = TelegramPointer(
            listOf(ChatPointer(1), ChatPointer(-2))
        )

        val update = pointer.update(ChatPointer(1, 100))
        assertEquals(100, update.getChatPointer(1)?.fromMessageId)
        assertEquals(0, update.getChatPointer(-2)?.fromMessageId)
    }

    @Test
    fun test_refresh() {
        val historyPointer = TelegramPointer(
            listOf(ChatPointer(1, 100), ChatPointer(-2))
        )

        val refreshedPointer = historyPointer.refreshChats(listOf(1, -3))
        assertEquals(2, refreshedPointer.pointers.size)
        assertEquals(100, refreshedPointer.getChatPointer(1)?.fromMessageId)
        assertEquals(0, refreshedPointer.getChatPointer(-3)?.fromMessageId)
    }
}