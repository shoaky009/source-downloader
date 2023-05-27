package xyz.shoaky.sourcedownloader.telegram

import it.tdlight.client.SimpleTelegramClient
import it.tdlight.jni.TdApi
import xyz.shoaky.sourcedownloader.sdk.OffsetPointer
import xyz.shoaky.sourcedownloader.sdk.OffsetSource
import xyz.shoaky.sourcedownloader.sdk.PointedItem
import xyz.shoaky.sourcedownloader.sdk.SourceItem
import java.net.URI
import java.time.Instant
import java.time.ZoneId

class TelegramSource(
    private val client: SimpleTelegramClient,
    private val chatId: Long,
) : OffsetSource {

    override fun fetch(pointer: OffsetPointer?, limit: Int): Iterable<PointedItem<OffsetPointer>> {
        val blockingResultHandler = BlockingResultHandler<TdApi.Messages>()
        val fromMessageId = pointer?.offset ?: 1
        val offset = -limit
        client.send(
            TdApi.GetChatHistory(chatId, fromMessageId, offset, limit, false),
            blockingResultHandler
        )

        val result = blockingResultHandler.future.join()
        val iterator = result.get().messages.map { message ->
            val sourceItem = messageToSourceItem(message) ?: return@map null
            PointedItem(sourceItem, OffsetPointer(message.id))
        }.filterNotNull().reversed()

        return iterator
    }

    private fun messageToSourceItem(message: TdApi.Message): SourceItem? {
        val info: FileMessage = when (val content = message.content) {
            is TdApi.MessageDocument -> {
                val document = content.document
                FileMessage(
                    document.fileName,
                    document.mimeType,
                    listOf(document.document.id)
                )
            }

            is TdApi.MessageVideo -> {
                val video = content.video
                FileMessage(
                    video.fileName,
                    video.mimeType,
                    listOf(video.video.id)
                )
            }

            is TdApi.MessageAudio -> {
                val audio = content.audio
                FileMessage(
                    audio.fileName,
                    audio.mimeType,
                    listOf(audio.audio.id)
                )
            }

            is TdApi.MessagePhoto -> {
                val photo = content.photo.sizes
                FileMessage(
                    content.caption.text,
                    photo.first().type,
                    photo.map { it.photo.id }
                )
            }

            else -> null
        } ?: return null

        val uri = URI("telegram://${message.chatId}/${message.id}")
        val downloadUri = URI("$uri?fileIds=${info.fileIds.joinToString(",")}")
        val messageDateTime = Instant.ofEpochSecond(message.date.toLong()).atZone(zoneId).toLocalDateTime()
        return SourceItem(info.subject, uri, messageDateTime, info.mimeType, downloadUri)
    }

    companion object {
        private val zoneId = ZoneId.systemDefault()
    }
}

private data class FileMessage(
    val subject: String,
    val mimeType: String,
    val fileIds: List<Int>
)

