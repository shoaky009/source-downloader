package xyz.shoaky.sourcedownloader.telegram.other

import telegram4j.core.MTProtoTelegramClient
import telegram4j.core.`object`.Message
import telegram4j.core.`object`.MessageMedia
import telegram4j.core.util.Id
import telegram4j.tl.ImmutableInputMessageID
import xyz.shoaky.sourcedownloader.sdk.PointedItem
import xyz.shoaky.sourcedownloader.sdk.SourceItem
import xyz.shoaky.sourcedownloader.sdk.SourceItemPointer
import xyz.shoaky.sourcedownloader.sdk.component.Source
import java.net.URI
import java.time.Duration
import java.time.ZoneId
import java.util.stream.IntStream
import kotlin.jvm.optionals.getOrDefault
import kotlin.jvm.optionals.getOrNull

class TelegramSource(
    private val client: MTProtoTelegramClient,
    private val chatIds: List<Long>,
) : Source<TelegramPointer> {

    override fun fetch(pointer: TelegramPointer?, limit: Int): Iterable<PointedItem<TelegramPointer>> {
        val telegramPointer = pointer?.refreshChats(chatIds) ?: TelegramPointer().refreshChats(chatIds)
        val chatPointers = telegramPointer.pointers

        val result: MutableList<PointedItem<TelegramPointer>> = mutableListOf()
        for (chatPointer in chatPointers) {
            val fromMessageId = chatPointer.fromMessageId
            val messages = client.getMessages(
                Id.ofChat(chatPointer.chatId),
                IntStream.range(fromMessageId, fromMessageId + limit)
                    .mapToObj { ImmutableInputMessageID.of(it) }.toList()
            ).blockOptional(Duration.ofSeconds(10L)).getOrNull()?.messages ?: return emptyList()

            val items = messages.mapNotNull { message ->
                val sourceItem = mediaMessageToSourceItem(message) ?: return@mapNotNull null
                val update = telegramPointer.update(chatPointer.copy(fromMessageId = message.id))
                PointedItem(sourceItem, update)
            }

            result.addAll(items)
            if (messages.size > limit) {
                break
            }
        }
        return result
    }

    private fun mediaMessageToSourceItem(message: Message): SourceItem? {
        val media = message.media.getOrNull() ?: return null
        val document = media as? MessageMedia.Document ?: return null
        val dc = document.document.get()
        val fileReferenceId = dc.fileReferenceId
        val uri = URI("telegram://?chatId=${message.chatId.asLong()}&messageId=${message.id}")
        val downloadUri = URI("$uri&fileIds=${fileReferenceId.documentId}")
        val messageDateTime = message.createTimestamp.atZone(zoneId).toLocalDateTime()
        return SourceItem(dc.fileName.getOrDefault(""), uri, messageDateTime, media.type.name, downloadUri)
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
            ChatPointer(it, chatLastMessage.getOrDefault(it, 1))
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
    var fromMessageId: Int,
)