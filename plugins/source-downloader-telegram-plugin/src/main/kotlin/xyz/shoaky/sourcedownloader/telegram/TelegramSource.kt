package xyz.shoaky.sourcedownloader.telegram

import it.tdlight.client.SimpleTelegramClient
import it.tdlight.jni.TdApi
import xyz.shoaky.sourcedownloader.sdk.OffsetPointer
import xyz.shoaky.sourcedownloader.sdk.PointedItem
import xyz.shoaky.sourcedownloader.sdk.SourceItem
import xyz.shoaky.sourcedownloader.sdk.SourceItemPointer
import xyz.shoaky.sourcedownloader.sdk.component.Source
import java.net.URI
import java.time.Instant
import java.time.ZoneId

class TelegramSource(
    private val client: SimpleTelegramClient,
    private val chatIds: List<Long>,
) : Source<TelegramPointer> {

    override fun fetch(pointer: TelegramPointer?, limit: Int): Iterable<PointedItem<TelegramPointer>> {
        val blockingResultHandler = BlockingResultHandler<TdApi.Messages>()
        val chatLastMessage =
            pointer?.pointers?.associateBy { it.chatId }?.mapValues { it.value.fromMessageId } ?: emptyMap()

        for (chatId in chatIds) {
            val fromMessageId = chatLastMessage.getOrDefault(chatId, 1L)
            val offset = -limit
            client.send(
                TdApi.GetChatHistory(chatIds.first(), fromMessageId, offset, limit, false),
                blockingResultHandler
            )

            val result = blockingResultHandler.future.join()
            val iterator = result.get().messages.map { message ->
                val sourceItem = mediaMessageToSourceItem(message) ?: return@map null
                PointedItem(sourceItem, OffsetPointer(message.id))
            }.filterNotNull().reversed()
        }

        return emptyList()
    }

    private fun mediaMessageToSourceItem(message: TdApi.Message): SourceItem? {
        val fileMessage = FileMessage.fromMessageContent(message.content) ?: return null

        val uri = URI("telegram://?chatId=${message.chatId}&messageId=${message.id}")
        val downloadUri = URI("$uri&fileIds=${fileMessage.fileIds.joinToString(",")}")
        val messageDateTime = Instant.ofEpochSecond(message.date.toLong()).atZone(zoneId).toLocalDateTime()
        return SourceItem(fileMessage.subject, uri, messageDateTime, fileMessage.mimeType, downloadUri)
    }

    companion object {
        private val zoneId = ZoneId.systemDefault()
    }
}


data class TelegramPointer(
    val pointers: List<ChatPointer> = emptyList()
) : SourceItemPointer

data class ChatPointer(
    val chatId: Long,
    val fromMessageId: Long,
)