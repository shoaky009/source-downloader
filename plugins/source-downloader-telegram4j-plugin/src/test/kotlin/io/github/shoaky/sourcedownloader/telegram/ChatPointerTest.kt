package io.github.shoaky.sourcedownloader.telegram

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import telegram4j.core.util.Id

class ChatPointerTest {
    @Test
    fun test() {
        val chatPointer = ChatPointer(1, -100)
        assertEquals(1, chatPointer.nextMessageId())
        assertEquals(101, chatPointer.copy(fromMessageId = 100).nextMessageId())

        assertEquals(11, ChatPointer(-11).parseChatId())

        assertEquals(Id.Type.CHAT, ChatPointer(11).createId().type)
        assertEquals(Id.Type.CHANNEL, ChatPointer(-11).createId().type)
    }

}