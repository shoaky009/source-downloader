package io.github.shoaky.sourcedownloader.telegram

import it.tdlight.Init
import it.tdlight.client.*
import it.tdlight.jni.TdApi
import it.tdlight.jni.TdApi.GetChatHistory
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.MockMakers
import org.mockito.Mockito.*
import kotlin.io.path.Path
import kotlin.test.assertEquals


class TelegramSourceTest {

    private val appId = 1
    private val appHash = "1"

    @Test
    @Disabled
    fun test() {
        val client = simpleTelegramClient()
        val source = TelegramSource(client, listOf(ChatConfig(-1001896478509), ChatConfig(-1001903190090)))
        val fetch = source.fetch(TelegramPointer(
            listOf(
                ChatPointer(-1001896478509, 29360128)
            )
        ), 99).toList()
        fetch.forEach {
            println(it)
        }
        println(fetch.size)
    }

    private fun simpleTelegramClient(): SimpleTelegramClient {
        Init.init()

        val proxy = TdApi.AddProxy("127.0.0.1", 8888, true, TdApi.ProxyTypeHttp())
        val settings = TDLibSettings.create(APIToken(appId, appHash))
        val clientFactory = SimpleTelegramClientFactory()

        settings.databaseDirectoryPath = Path("src", "test", "resources")
        settings.downloadedFilesDirectoryPath = Path("src", "test", "resources", "downloads")

        val client = clientFactory.builder(settings).build(AuthenticationSupplier.qrCode())
        client.send(proxy) {}
        return client
    }

    @Test
    @Disabled
    fun name() {
        val client = mock(SimpleTelegramClient::class.java, withSettings().serializable().mockMaker(MockMakers.INLINE))
        val source = TelegramSource(client, listOf(ChatConfig(1), ChatConfig(2), ChatConfig(3)))

        `when`(
            client.send(eq(GetChatHistory())) {
                TdApi.Messages().messages = arrayOf()
            }
        ).thenReturn(Unit)
        val pointer = TelegramPointer(
            listOf(
                ChatPointer(1, 1),
                ChatPointer(2, 1),
                ChatPointer(3, 1)
            )
        )
        val fetch = source.fetch(pointer)

    }

    @Test
    fun test_refresh_chat_pointer() {
        val telegramPointer = TelegramPointer(listOf(ChatPointer(2, 5)))
        val pointers = telegramPointer.refreshChats(listOf(1, 2, 3)).pointers
        assertEquals(pointers.size, 3)
        assertEquals(pointers.first { it.chatId == 2L }.fromMessageId, 5L)
    }
}