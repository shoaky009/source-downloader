package xyz.shoaky.sourcedownloader.telegram

import it.tdlight.Init
import it.tdlight.client.*
import it.tdlight.jni.TdApi
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import xyz.shoaky.sourcedownloader.sdk.PluginContext
import xyz.shoaky.sourcedownloader.sdk.Properties
import kotlin.io.path.Path

// @Disabled
class TelegramSourceTest {

    val appId = 1
    val appHash = ""

    @Test
    fun test() {
        val client = simpleTelegramClient()
        val pluginContext = Mockito.mock(PluginContext::class.java)
        Mockito.`when`(pluginContext.load("", SimpleTelegramClient::class.java))
            .thenReturn(client)
        val source = TelegramSourceSupplier(pluginContext).apply(
            Properties.fromMap(
                mapOf(
                    "chat-id" to -1001896478509,
                    "client" to "",
                )
            )
        )

        val fetch = source.fetch(null)
        fetch.toList().forEach {
            println(it)
        }

    }

    private fun simpleTelegramClient(): SimpleTelegramClient {
        Init.init()

        val proxy = TdApi.AddProxy("127.0.0.1", 7890, true, TdApi.ProxyTypeHttp())
        val settings = TDLibSettings.create(APIToken(appId, appHash))
        val clientFactory = SimpleTelegramClientFactory()

        settings.databaseDirectoryPath = Path("src", "test", "resources")
        settings.downloadedFilesDirectoryPath = Path("src", "test", "resources", "downloads")

        val client = clientFactory.builder(settings).build(AuthenticationSupplier.qrCode())
        client.send(proxy) {}
        return client
    }
}