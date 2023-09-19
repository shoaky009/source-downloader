package io.github.shoaky.sourcedownloader.telegram

import com.fasterxml.jackson.annotation.JsonAlias
import io.github.shoaky.sourcedownloader.sdk.PointedItem
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.Source
import io.github.shoaky.sourcedownloader.sdk.util.ExpandIterator
import io.github.shoaky.sourcedownloader.sdk.util.RequestResult
import telegram4j.tl.*
import telegram4j.tl.messages.BaseMessages
import telegram4j.tl.messages.ChannelMessages
import telegram4j.tl.request.messages.ImmutableGetHistory
import java.net.URI
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * 从头迭代Telegram给定的chatId的文件消息
 */
class TelegramSource(
    private val messageFetcher: TelegramMessageFetcher,
    private val chats: List<ChatConfig>,
) : Source<TelegramPointer> {

    override fun fetch(pointer: TelegramPointer, limit: Int): Iterable<PointedItem<ChatPointer>> {
        pointer.refreshChats(chats.map { it.chatId })
        val chatMapping = chats.associateBy { it.chatId }
        val chatPointers = pointer.chatLastMessageIds.map { (chatId, messageId) ->
            ChatPointer(chatId, messageId)
        }

        return ExpandIterator(chatPointers, limit) { chatPointer ->
            val beginDate = chatMapping[chatPointer.chatId]?.beginDate
            val messages = messageFetcher.fetchMessages(chatPointer, limit, timeout)
            if (messages.isEmpty()) {
                return@ExpandIterator RequestResult(emptyList(), true)
            }

            val items = messages.mapNotNull { message ->
                val sourceItem = mediaMessageToSourceItem(message, chatPointer) ?: return@mapNotNull null
                PointedItem(sourceItem, chatPointer.copy(fromMessageId = message.id()))
            }.filter { beginDate == null || beginDate <= it.sourceItem.date.toLocalDate() }
            chatPointer.fromMessageId = messages.last().id()
            RequestResult(items)
        }.asIterable()
    }

    private fun mediaMessageToSourceItem(message: BaseMessage, chatPointer: ChatPointer): SourceItem? {
        val media = message.media() ?: return null
        val chatId = chatPointer.parseChatId()
        val uri = URI("telegram://?chatId=${chatPointer.chatId}&messageId=${message.id()}")
        val messageDateTime = Instant.ofEpochSecond(message.date().toLong()).atZone(zoneId).toLocalDateTime()
        when (media) {
            is MessageMediaPhoto -> {
                return SourceItem("$chatId-${message.id()}.jpg", uri,
                    messageDateTime, "image/jpg", uri,
                    attrs = mapOf(MEDIA_TYPE_ATTR to "photo"))
            }

            is MessageMediaDocument -> {
                val document = media as? MessageMediaDocument ?: return null
                val dc = document.document() as? ImmutableBaseDocument ?: return null
                val filename = dc.attributes()
                    .filterIsInstance<DocumentAttributeFilename>()
                    .firstOrNull()?.fileName() ?: "$chatId-${message.id()}"
                return SourceItem(filename, uri, messageDateTime, dc.mimeType(), uri, mapOf(MEDIA_TYPE_ATTR to "document"))
            }

            else -> return null
        }
    }

    companion object {

        private val zoneId = ZoneId.systemDefault()
        private val timeout: Duration = Duration.ofSeconds(5L)
        const val MEDIA_TYPE_ATTR = "mediaType"
    }

    override fun defaultPointer(): TelegramPointer {
        return TelegramPointer()
    }
}

data class ChatConfig(
    @JsonAlias("chat-id")
    val chatId: Long,
    @JsonAlias("begin-date")
    val beginDate: LocalDate? = null
)

interface TelegramMessageFetcher {

    fun fetchMessages(chatPointer: ChatPointer, limit: Int, timeout: Duration): List<BaseMessage>
}

class DefaultMessageFetcher(
    private val wrapper: TelegramClientWrapper,
) : TelegramMessageFetcher {

    private val client = wrapper.client

    override fun fetchMessages(chatPointer: ChatPointer, limit: Int, timeout: Duration): List<BaseMessage> {
        val isChannel = chatPointer.isChannel()
        val getHistoryBuilder = ImmutableGetHistory.builder()
            .offsetId(chatPointer.nextMessageId())
            .addOffset(-limit)
            .limit(limit)
            .maxId(-1)
            .minId(-1)
            .hash(0)
            .offsetDate(0)

        val inputPeer: InputPeer = if (isChannel) {
            val user = client.getUserMinById(client.selfId).blockOptional(timeout).get()
            val inputChannel = ImmutableBaseInputChannel.builder()
                .channelId(chatPointer.parseChatId())
                .accessHash(user.id.accessHash.get())
                .build()
            val channel =
                client.serviceHolder.chatService.getChannel(inputChannel).blockOptional(timeout).get() as Channel
            InputPeerChannel.builder()
                .channelId(channel.id())
                .accessHash(channel.accessHash()!!)
                .build()
        } else {
            InputPeerChat.builder()
                .chatId(chatPointer.parseChatId())
                .build()
        }
        val getHistory = getHistoryBuilder.peer(inputPeer).build()
        val historyMessage = client.serviceHolder.chatService.getHistory(getHistory)
            .blockOptional(timeout).get()
        if (historyMessage is ChannelMessages) {
            return historyMessage.messages().filterIsInstance<BaseMessage>().reversed()
        }
        if (historyMessage is BaseMessages) {
            return historyMessage.messages().filterIsInstance<BaseMessage>().reversed()
        }
        return emptyList()
    }

}