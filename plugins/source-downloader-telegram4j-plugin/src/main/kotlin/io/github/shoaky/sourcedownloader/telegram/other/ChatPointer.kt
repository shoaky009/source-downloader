package io.github.shoaky.sourcedownloader.telegram.other

import com.fasterxml.jackson.annotation.JsonIgnore
import telegram4j.core.util.Id
import kotlin.math.abs

data class ChatPointer(
    val chatId: Long,
    var fromMessageId: Int,
) {

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
}