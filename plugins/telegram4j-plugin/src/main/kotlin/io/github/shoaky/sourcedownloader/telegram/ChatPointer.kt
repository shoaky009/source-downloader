package io.github.shoaky.sourcedownloader.telegram

import com.fasterxml.jackson.annotation.JsonIgnore
import io.github.shoaky.sourcedownloader.sdk.ItemPointer
import telegram4j.core.util.Id
import kotlin.math.abs

data class ChatPointer(
    val chatId: Long,
    var fromMessageId: Int,
) : ItemPointer {

    constructor(chatId: Long) : this(chatId, 0)

    private val realChatId = abs(chatId)

    @JsonIgnore
    fun isChannel(): Boolean = chatId < 0
    fun parseChatId(): Long = realChatId

    fun createId(): Id {
        return if (isChannel()) {
            Id.ofChannel(parseChatId())
        } else {
            Id.ofChat(parseChatId())
        }
    }

    fun nextMessageId(): Int {
        return maxOf(fromMessageId + 1, MIN_MESSAGE_ID)
    }

    private companion object {

        const val MIN_MESSAGE_ID = 1
    }
}