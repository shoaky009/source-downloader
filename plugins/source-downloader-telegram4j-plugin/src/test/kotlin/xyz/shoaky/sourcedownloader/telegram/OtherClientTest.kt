package xyz.shoaky.sourcedownloader.telegram

import io.netty.util.ResourceLeakDetector
import org.junit.jupiter.api.Test
import reactor.core.publisher.Hooks
import telegram4j.core.util.Id
import telegram4j.tl.ImmutableInputMessageID
import telegram4j.tl.InputMessage
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.telegram.other.TelegramClientInstanceFactory
import xyz.shoaky.sourcedownloader.telegram.other.TelegramPointer
import xyz.shoaky.sourcedownloader.telegram.other.TelegramSource
import java.util.stream.IntStream


// @Disabled
class OtherClientTest {


    @Test
    fun name() {
        Hooks.onOperatorDebug()
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID)

        val client = TelegramClientInstanceFactory.create(
            Properties.fromMap(
                mapOf(
                    "api-id" to "1",
                    "api-hash" to "1",
                    "proxy" to "http://localhost:8888",
                    "metadata-path" to "src/test/resources"
                )
            )
        )

        client.getMessages(Id.ofChat(-1001896478509L), listOf(ImmutableInputMessageID.of(1)))
            .blockOptional().get()
            .messages.forEach {
                println(it)
            }
        // val source = TelegramSource(client, listOf(-1001724819878))
        //
        // val fetch = source.fetch(null).toList()
        // fetch.forEach {
        //     println(it)
        // }
    }
}