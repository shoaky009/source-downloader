package io.github.shoaky.sourcedownloader.telegram

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import telegram4j.tl.ImmutableBaseMessage
import telegram4j.tl.ImmutablePeerChat
import java.time.Duration
import java.util.stream.IntStream

@Disabled
class TelegramSourceTest {

    @Test
    fun normal() {
        val fetcher = Mockito.mock(TelegramMessageFetcher::class.java)

        Mockito.`when`(
            fetcher.fetchMessages(
                ChatPointer(1),
                50,
                Mockito.eq(Duration.ofSeconds(5L))
            )
        ).thenAnswer { _ ->
            IntStream.range(1, 50)
                .mapToObj {
                    val of = ImmutablePeerChat.of(1)
                    val l = System.currentTimeMillis() / 1000
                    ImmutableBaseMessage.of(it, it, of, l.toInt(), it.toString())
                }
        }
        val telegramSource = TelegramSource(fetcher, listOf(ChatConfig(1)))
        val fetch = telegramSource.fetch(TelegramPointer())
        println(fetch)
    }
}