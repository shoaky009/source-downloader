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
    private val chatId: Long
) : OffsetSource {

    override fun fetch(pointer: OffsetPointer?, limit: Int): Iterable<PointedItem<OffsetPointer>> {
        client.send(TdApi.GetChatHistory(chatId, 0, 0, limit, false)) {
            val iterator = it.get().messages.asSequence().map { message ->
                val sourceItem = messageToSourceItem(message)
                PointedItem(sourceItem, OffsetPointer(message.id))
            }.asIterable()
        }
        return emptyList()
    }

    private fun messageToSourceItem(message: TdApi.Message): SourceItem {
        val uri = URI("telegram://${message.chatId}/${message.id}")
        val messageDateTime = Instant.ofEpochSecond(message.date.toLong()).atZone(zoneId)
            .toLocalDateTime()
        return SourceItem(message.content.toString(), uri, messageDateTime, "telegram/message", uri)
    }

    companion object {
        private val zoneId = ZoneId.systemDefault()
    }

}