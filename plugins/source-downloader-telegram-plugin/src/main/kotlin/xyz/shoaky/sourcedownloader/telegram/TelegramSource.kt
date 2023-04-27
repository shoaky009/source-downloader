package xyz.shoaky.sourcedownloader.telegram

import it.tdlight.client.SimpleTelegramClient
import it.tdlight.jni.TdApi
import xyz.shoaky.sourcedownloader.sdk.SourceItem
import xyz.shoaky.sourcedownloader.sdk.component.Source
import java.net.URI
import java.time.Instant
import java.time.ZoneId

class TelegramSource(
    private val client: SimpleTelegramClient,
    private val chatId: Long
) : Source {

    // TODO fetch要调整，这样写虽然不会重复下载，但是会重复读取
    override fun fetch(): Iterable<SourceItem> {
        // 还没看是懒加载消息还是一次性加载所有消息
        // offset/beginMessageId 也待定
        val beginMessageId = 0L
        val offset = 100
        val limit = 50
        client.send(TdApi.GetChatHistory(chatId, beginMessageId, offset, limit, false)) {
            val totalCount = it.get().totalCount
            val iterator = it.get().messages.asSequence().map { message ->
                messageToSourceItem(message)
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