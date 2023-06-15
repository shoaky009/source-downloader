package io.github.shoaky.sourcedownloader.telegram

import io.github.shoaky.sourcedownloader.sdk.SourceItemPointer

data class TelegramPointer(
    val pointers: List<ChatPointer> = emptyList()
) : SourceItemPointer {

    fun refreshChats(chatIds: List<Long>): TelegramPointer {
        val chatLastMessage = pointers.associateBy { it.chatId }.mapValues { it.value.fromMessageId }
        val chatPointers = chatIds.map {
            ChatPointer(it, chatLastMessage.getOrDefault(it, 0))
        }
        return TelegramPointer(chatPointers)
    }

    fun update(chatPointer: ChatPointer): TelegramPointer {
        val updateChats = pointers.toMutableList()
        updateChats.replaceAll { pointer ->
            if (pointer.parseChatId() == chatPointer.parseChatId()) {
                chatPointer
            } else {
                pointer
            }
        }
        return TelegramPointer(updateChats)
    }

    fun getChatPointer(chatId: Long): ChatPointer? {
        return pointers.firstOrNull { it.chatId == chatId }
    }
}