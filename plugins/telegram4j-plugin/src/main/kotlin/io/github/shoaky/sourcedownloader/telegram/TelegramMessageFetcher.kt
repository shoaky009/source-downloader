package io.github.shoaky.sourcedownloader.telegram

import reactor.core.publisher.Mono
import telegram4j.core.`object`.chat.Chat
import telegram4j.core.util.Id
import telegram4j.mtproto.MTProtoRetrySpec
import telegram4j.tl.*
import telegram4j.tl.messages.BaseMessages
import telegram4j.tl.messages.ChannelMessages
import telegram4j.tl.request.messages.ImmutableGetHistory
import java.time.Duration

class TelegramMessageFetcher(
    val wrapper: TelegramClientWrapper,
) {

    fun fetchMessages(chatPointer: ChatPointer, limit: Int, timeout: Duration): List<BaseMessage> {
        val isChannel = chatPointer.isChannel()
        val getHistoryBuilder = ImmutableGetHistory.builder()
            .offsetId(chatPointer.nextMessageId())
            .addOffset(-limit)
            .limit(limit)
            .maxId(-1)
            .minId(-1)
            .hash(0)
            .offsetDate(0)

        val client = wrapper.getClient()
        val inputPeer: InputPeer = if (isChannel) {
            val user = client.getUserMinById(client.selfId)
                .onErrorMap {
                    wrapRetryableExceptionIfNeeded(it)
                }
                .blockOptional(timeout).get()
            val inputChannel = ImmutableBaseInputChannel.builder()
                .channelId(chatPointer.parseChatId())
                .accessHash(user.id.accessHash.get())
                .build()
            val channel = try {
                client.serviceHolder.chatService.getChannel(inputChannel)
                    .onErrorMap {
                        wrapRetryableExceptionIfNeeded(it)
                    }
                    .blockOptional(timeout).get() as Channel
            } catch (e: Exception) {
                throw IllegalStateException("Failed to get channel info for ${chatPointer.chatId}", e)
            }
            InputPeerChannel.builder()
                .channelId(channel.id())
                .accessHash(channel.accessHash() ?: throw IllegalStateException("Access hash missing"))
                .build()
        } else {
            InputPeerChat.builder()
                .chatId(chatPointer.parseChatId())
                .build()
        }
        val getHistory = getHistoryBuilder.peer(inputPeer).build()
        val historyMessage = client.serviceHolder.chatService.getHistory(getHistory)
            .retryWhen(
                MTProtoRetrySpec.max(2)
            )
            .blockOptional(timeout).get()
        if (historyMessage is ChannelMessages) {
            return historyMessage.messages().filterIsInstance<BaseMessage>().reversed()
        }
        if (historyMessage is BaseMessages) {
            return historyMessage.messages().filterIsInstance<BaseMessage>().reversed()
        }
        return emptyList()
    }

    fun getChatMinById(id: Id): Mono<Chat> {
        return wrapper.getClient().getChatMinById(id)
    }

}