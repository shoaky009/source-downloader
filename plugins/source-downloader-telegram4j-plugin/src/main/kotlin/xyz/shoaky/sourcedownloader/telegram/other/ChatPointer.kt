package xyz.shoaky.sourcedownloader.telegram.other

import telegram4j.core.util.Id
import kotlin.math.abs

data class ChatPointer(
    private val chatId: Long,
    var fromMessageId: Int,
) {

    private val realChatId = abs(chatId)

    fun isChannel(): Boolean = chatId < 0
    fun getChatId(): Long = realChatId

    fun getRawChatId() = chatId

    fun createId(): Id {
        return if (isChannel()) {
            Id.ofChannel(getChatId())
        } else {
            Id.ofChat(getChatId())
        }
    }
}