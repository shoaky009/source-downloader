package io.github.shoaky.sourcedownloader.telegram

import io.github.shoaky.sourcedownloader.sdk.ItemPointer
import io.github.shoaky.sourcedownloader.sdk.SourcePointer

data class TelegramPointer(
    // val pointers: List<ChatPointer> = emptyList(),
    val chatLastMessageIds: MutableMap<Long, Int> = mutableMapOf()
) : SourcePointer {

    fun refreshChats(chatIds: List<Long>) {
        val chatLastMessage = chatLastMessageIds.toMap()
        chatLastMessageIds.clear()
        chatIds.forEach {
            chatLastMessageIds[it] = chatLastMessage[it] ?: 0
        }
    }

    fun getLastMessageId(chatId: Long): Int? {
        return chatLastMessageIds[chatId]
    }

    override fun update(itemPointer: ItemPointer) {
        if (itemPointer is ChatPointer) {
            chatLastMessageIds[itemPointer.chatId] = itemPointer.fromMessageId
        }
    }
}