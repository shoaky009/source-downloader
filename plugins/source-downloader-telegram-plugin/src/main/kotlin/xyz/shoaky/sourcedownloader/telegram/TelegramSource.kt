package io.github.shoaky.sourcedownloader.telegram

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnore
import it.tdlight.client.SimpleTelegramClient
import it.tdlight.jni.TdApi
import io.github.shoaky.sourcedownloader.sdk.PointedItem
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.SourceItemPointer
import io.github.shoaky.sourcedownloader.sdk.component.Source
import java.net.URI
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs

class TelegramSource(
    private val client: SimpleTelegramClient,
    private val chats: List<ChatConfig>,
) : Source<TelegramPointer> {

    override fun fetch(pointer: TelegramPointer?, limit: Int): Iterable<PointedItem<TelegramPointer>> {
        val chatMapping = chats.associateBy { it.chatId }
        val chatIds = chatMapping.keys.toList()
        val telegramPointer = pointer?.refreshChats(chatIds) ?: TelegramPointer().refreshChats(chatIds)
        val chatPointers = telegramPointer.pointers

        val result: MutableList<PointedItem<TelegramPointer>> = mutableListOf()
        for (chatPointer in chatPointers) {
            val fromMessageId = chatPointer.fromMessageId
            val beginDate = chatMapping[chatPointer.chatId]?.beginDate
            val offset = -limit
            val blockingResultHandler = BlockingResultHandler<TdApi.Messages>()
            client.send(
                // fromMessageId代表上一次已经完成的，所以要跳过这一条需要+1
                TdApi.GetChatHistory(chatPointer.chatId, fromMessageId + 1, offset, limit, false),
                blockingResultHandler
            )

            val messages = blockingResultHandler.get().messages.map { message ->
                val sourceItem = mediaMessageToSourceItem(message) ?: return@map null
                val update = telegramPointer.update(chatPointer.copy(fromMessageId = message.id))
                PointedItem(sourceItem, update)
            }
                .filterNotNull()
                .filter { beginDate == null || beginDate <= it.sourceItem.date.toLocalDate() }
                .reversed()
            result.addAll(messages)
            if (messages.size > limit) {
                break
            }
        }
        return result
    }

    private fun mediaMessageToSourceItem(message: TdApi.Message): SourceItem? {
        val fileMessage = FileMessage.fromMessageContent(message) ?: return null

        val uri = URI("telegram://?chatId=${message.chatId}&messageId=${message.id}")
        val downloadUri = URI("$uri&fileId=${fileMessage.fileId}")
        val messageDateTime = Instant.ofEpochSecond(message.date.toLong()).atZone(zoneId).toLocalDateTime()
        return SourceItem(fileMessage.subject, uri, messageDateTime, fileMessage.mimeType, downloadUri)
    }

    companion object {
        private val zoneId = ZoneId.systemDefault()
    }
}


data class TelegramPointer(
    val pointers: List<ChatPointer> = emptyList()
) : SourceItemPointer {

    fun refreshChats(chatIds: List<Long>): TelegramPointer {
        val chatLastMessage = pointers.associateBy { it.chatId }.mapValues { it.value.fromMessageId }
        val chatPointers = chatIds.map {
            ChatPointer(it, chatLastMessage.getOrDefault(it, 0L))
        }
        return TelegramPointer(chatPointers)
    }

    fun update(chatPointer: ChatPointer): TelegramPointer {
        val updateChats = pointers.toMutableList()
        updateChats.replaceAll { pointer ->
            if (pointer.chatId == chatPointer.chatId) {
                chatPointer
            } else {
                pointer
            }
        }
        return TelegramPointer(updateChats)
    }
}

data class ChatPointer(
    val chatId: Long,
    var fromMessageId: Long,
) {

    constructor(chatId: Long) : this(chatId, 0)

    private val realChatId = abs(chatId)

    @JsonIgnore
    fun isChannel(): Boolean = chatId < 0
    fun parseChatId(): Long = realChatId

}

data class ChatConfig(
    @JsonAlias("chat-id")
    val chatId: Long,
    @JsonAlias("begin-date")
    val beginDate: LocalDate? = null
)