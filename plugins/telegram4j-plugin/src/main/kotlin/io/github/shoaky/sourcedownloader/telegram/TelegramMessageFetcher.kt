package io.github.shoaky.sourcedownloader.telegram

import telegram4j.tl.*
import telegram4j.tl.messages.BaseMessages
import telegram4j.tl.messages.ChannelMessages
import telegram4j.tl.request.messages.ImmutableGetHistory
import java.time.Duration

class TelegramMessageFetcher(
    wrapper: TelegramClientWrapper,
) {

    val client = wrapper.client

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