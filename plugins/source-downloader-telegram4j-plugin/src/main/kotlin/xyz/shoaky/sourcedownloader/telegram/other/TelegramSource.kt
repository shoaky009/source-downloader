package xyz.shoaky.sourcedownloader.telegram.other

import com.fasterxml.jackson.annotation.JsonAlias
import telegram4j.core.MTProtoTelegramClient
import telegram4j.tl.*
import telegram4j.tl.messages.BaseMessages
import telegram4j.tl.messages.ChannelMessages
import telegram4j.tl.request.messages.ImmutableGetHistory
import xyz.shoaky.sourcedownloader.sdk.PointedItem
import xyz.shoaky.sourcedownloader.sdk.SourceItem
import xyz.shoaky.sourcedownloader.sdk.component.Source
import java.net.URI
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class TelegramSource(
    private val client: MTProtoTelegramClient,
    private val chats: List<ChatConfig>,
) : Source<TelegramPointer> {

    override fun fetch(pointer: TelegramPointer?, limit: Int): Iterable<PointedItem<TelegramPointer>> {
        val chatMapping = chats.associateBy { it.chatId }
        val chatIds = chatMapping.keys.toList()
        val telegramPointer = pointer?.refreshChats(chatIds) ?: TelegramPointer().refreshChats(chatIds)
        val chatPointers = telegramPointer.pointers

        val result: MutableList<PointedItem<TelegramPointer>> = mutableListOf()
        for (chatPointer in chatPointers) {
            val beginDate = chatMapping[chatPointer.getRawChatId()]?.beginDate
            val messages = getMessages(chatPointer, limit)
            val items = messages.mapNotNull { message ->
                val sourceItem = mediaMessageToSourceItem(message, chatPointer) ?: return@mapNotNull null
                val update = telegramPointer.update(chatPointer.copy(fromMessageId = message.id()))
                PointedItem(sourceItem, update)
            }.filter { beginDate == null || beginDate <= it.sourceItem.date.toLocalDate() }
            result.addAll(items)
            if (messages.size > limit) {
                break
            }
        }
        return result
    }

    private fun getMessages(chatPointer: ChatPointer, limit: Int): List<BaseMessage> {
        val isChannel = chatPointer.isChannel()
        val getHistoryBuilder = ImmutableGetHistory.builder()
            .offsetId(chatPointer.fromMessageId)
            .addOffset(-limit)
            .limit(limit)
            .maxId(-1)
            .minId(-1)
            .hash(0)
            .offsetDate(0)

        val inputPeer: InputPeer = if (isChannel) {
            val user = client.getUserMinById(client.selfId).blockOptional().get()
            val inputChannel = ImmutableBaseInputChannel.builder()
                .channelId(chatPointer.getChatId())
                .accessHash(user.id.accessHash.get())
                .build()
            val channel = client.serviceHolder.chatService.getChannel(inputChannel).blockOptional().get() as Channel
            InputPeerChannel.builder()
                .channelId(channel.id())
                .accessHash(channel.accessHash()!!)
                .build()
        } else {
            InputPeerChat.builder()
                .chatId(chatPointer.getChatId())
                .build()
        }
        val getHistory = getHistoryBuilder.peer(inputPeer).build()
        val historyMessage = client.serviceHolder.chatService.getHistory(getHistory)
            .blockOptional(Duration.ofSeconds(10)).get()
        if (historyMessage is ChannelMessages) {
            return historyMessage.messages().filterIsInstance<BaseMessage>().reversed()
        }
        if (historyMessage is BaseMessages) {
            return historyMessage.messages().filterIsInstance<BaseMessage>().reversed()
        }
        return emptyList()
    }

    private fun mediaMessageToSourceItem(message: BaseMessage, chatPointer: ChatPointer): SourceItem? {
        val media = message.media() ?: return null
        val chatId = chatPointer.getChatId()
        val uri = URI("telegram://?chatId=${chatPointer.getRawChatId()}&messageId=${message.id()}")
        val messageDateTime = Instant.ofEpochSecond(message.date().toLong()).atZone(zoneId).toLocalDateTime()
        when (media) {
            is MessageMediaPhoto -> {
                return SourceItem("$chatId-${message.id()}", uri, messageDateTime, "image/jpg", uri)
            }

            is MessageMediaDocument -> {
                val document = media as? MessageMediaDocument ?: return null
                val dc = document.document() as? ImmutableBaseDocument ?: return null
                val filename = dc.attributes()
                    .filterIsInstance<DocumentAttributeFilename>()
                    .firstOrNull()?.fileName() ?: "$chatId-${message.id()}"
                return SourceItem(filename, uri, messageDateTime, dc.mimeType(), uri)
            }

            else -> return null
        }
    }

    companion object {
        private val zoneId = ZoneId.systemDefault()
    }
}


data class ChatConfig(
    @JsonAlias("chat-id")
    val chatId: Long,
    @JsonAlias("begin-date")
    val beginDate: LocalDate? = null
)