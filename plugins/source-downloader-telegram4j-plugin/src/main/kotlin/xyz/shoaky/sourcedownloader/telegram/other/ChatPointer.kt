package xyz.shoaky.sourcedownloader.telegram.other

import com.fasterxml.jackson.annotation.JsonIgnore
import telegram4j.core.util.Id
import kotlin.math.abs

data class ChatPointer(
    val chatId: Long,
    var fromMessageId: Int,
) {

    private val realChatId = abs(chatId)

    @JsonIgnore
    fun isChannel(): Boolean = chatId < 0
    fun paserChatId(): Long = realChatId

    fun createId(): Id {
        return if (isChannel()) {
            Id.ofChannel(paserChatId())
        } else {
            Id.ofChat(paserChatId())
        }
    }
}