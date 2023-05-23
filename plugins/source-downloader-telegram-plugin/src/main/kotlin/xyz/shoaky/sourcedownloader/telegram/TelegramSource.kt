package xyz.shoaky.sourcedownloader.telegram

import it.tdlight.client.GenericResultHandler
import it.tdlight.client.Result
import it.tdlight.client.SimpleTelegramClient
import it.tdlight.jni.TdApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
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
        val blockingResultHandler = BlockingResultHandler()
        client.send(
            TdApi.GetChatHistory(chatId, 0, 0, limit, false),
            blockingResultHandler
        )

        val result = blockingResultHandler.get()
        val iterator = result.get().messages.asSequence().map { message ->
            val sourceItem = messageToSourceItem(message)
            PointedItem(sourceItem, OffsetPointer(message.id))
        }.asIterable()

        return iterator
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

    class BlockingResultHandler(
        private val timeout: Long = 10000
    ) : GenericResultHandler<TdApi.Messages> {

        private lateinit var result: Result<TdApi.Messages>
        private var job: Job? = null
        override fun onResult(result: Result<TdApi.Messages>) {
            this.result = result
        }

        fun get(): Result<TdApi.Messages> = runBlocking {
            if (job == null) {
                job = launch {
                    while (!::result.isInitialized) {
                        Thread.onSpinWait()
                    }
                }
            }

            withTimeoutOrNull(timeout) {
                job?.join()
                this@BlockingResultHandler.result
            }

            job?.cancel()
            result
        }
    }

}