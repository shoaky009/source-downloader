package io.github.shoaky.sourcedownloader.telegram

import io.github.shoaky.sourcedownloader.sdk.PointedItem
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.Source
import io.github.shoaky.sourcedownloader.sdk.util.ExpandIterator
import io.github.shoaky.sourcedownloader.sdk.util.IterationResult
import telegram4j.tl.*
import java.net.URI
import java.time.Duration
import java.time.Instant

/**
 * 从头迭代Telegram给定的chatId的文件消息
 */
class TelegramSource(
    private val messageFetcher: TelegramMessageFetcher,
    private val chats: List<ChatConfig>,
    private val sites: Set<String> = setOf("Telegraph"),
    private val includeNonMedia: Boolean = false
) : Source<TelegramPointer> {

    override fun fetch(pointer: TelegramPointer, limit: Int): Iterable<PointedItem<ChatPointer>> {
        pointer.refreshChats(chats.map { it.chatId })
        val chatMapping = chats.associateBy { it.chatId }
        val chatPointers = pointer.chatLastMessageIds.map { (chatId, messageId) ->
            ChatPointer(chatId, messageId)
        }
        val telegramClient = messageFetcher.client
        return ExpandIterator(chatPointers, limit) { chatPointer ->
            val beginDate = chatMapping[chatPointer.chatId]?.beginDate
            val messages = messageFetcher.fetchMessages(chatPointer, limit, timeout)
            if (messages.isEmpty()) {
                return@ExpandIterator IterationResult(emptyList(), true)
            }

            val chat = telegramClient.getChatMinById(ChatPointer(chatPointer.chatId).createId())
                .blockOptional(timeout).get()
            val items = messages.mapNotNull { message ->
                val sourceItem = mediaMessageToSourceItem(message, chatPointer, chat.name) ?: return@mapNotNull null
                PointedItem(sourceItem, chatPointer.copy(fromMessageId = message.id()))
            }.filter { beginDate == null || beginDate <= it.sourceItem.datetime.toLocalDate() }
            chatPointer.fromMessageId = messages.last().id()
            IterationResult(items)
        }.asIterable()
    }

    private fun mediaMessageToSourceItem(
        message: BaseMessage,
        chatPointer: ChatPointer,
        chatName: String
    ): SourceItem? {
        val messageId = message.id()
        val chatId = chatPointer.parseChatId()
        val link = URI("tg://privatepost?channel=$chatId&post=$messageId")
        val downloadUri = URI("tg://privatepost?channel=${chatPointer.chatId}&post=$messageId")
        val messageDateTime = Instant.ofEpochSecond(message.date().toLong()).atOffset(SourceItem.DEFAULT_OFFSET)
        val media = message.media()
        val attrs = mutableMapOf(
            "messageId" to messageId,
            "chatId" to chatId,
            "chatName" to chatName
        )
        if (includeNonMedia && media == null) {
            return SourceItem(
                "message-$messageId",
                link,
                messageDateTime,
                "message",
                downloadUri,
                attrs
            )
        }
        when (media) {
            is MessageMediaPhoto -> {
                attrs[MEDIA_TYPE_ATTR] = "photo"
                convertMedia(media)?.size()?.also {
                    attrs["size"] = it
                }
                return SourceItem(
                    "$chatId-$messageId.jpg", link,
                    messageDateTime, "image/jpg", downloadUri,
                    attrs = attrs
                )
            }

            is MessageMediaDocument -> {
                val document = convertMedia(media) ?: return null
                val filename = document.attributes()
                    .filterIsInstance<DocumentAttributeFilename>()
                    .firstOrNull()?.fileName() ?: "$chatId-${message.id()}"
                attrs[MEDIA_TYPE_ATTR] = "document"
                attrs["size"] = document.size()
                return SourceItem(
                    filename,
                    link,
                    messageDateTime,
                    document.mimeType(),
                    downloadUri,
                    attrs
                )
            }

            is MessageMediaWebPage -> {
                attrs[MEDIA_TYPE_ATTR] = "webpage"
                // 会有WebPageEmpty的情况
                val webpage = media.webpage() as? BaseWebPage ?: return null
                val siteName = webpage.siteName() ?: return null
                if (sites.contains(siteName).not()) {
                    log.debug("Ignore site: $siteName")
                    return null
                }
                webpage.siteName()?.also {
                    attrs["site"] = it
                }
                return SourceItem(
                    webpage.title() ?: message.message(),
                    link,
                    messageDateTime,
                    webpage.type() ?: "webpage",
                    URI(webpage.url()),
                    attrs
                )
            }

            else -> return null
        }
    }

    private fun convertMedia(media: MessageMedia): ImmutableBaseDocument? {
        val document = media as? MessageMediaDocument ?: return null
        return document.document() as? ImmutableBaseDocument ?: return null
    }

    companion object {

        private val timeout: Duration = Duration.ofSeconds(5L)
        const val MEDIA_TYPE_ATTR = "mediaType"
    }

    override fun defaultPointer(): TelegramPointer {
        return TelegramPointer()
    }
}