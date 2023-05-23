package xyz.shoaky.sourcedownloader.telegram

import it.tdlight.Init
import it.tdlight.client.APIToken
import it.tdlight.client.AuthenticationSupplier
import it.tdlight.client.SimpleTelegramClientFactory
import it.tdlight.client.TDLibSettings
import it.tdlight.jni.TdApi
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

@Disabled
class TelegramSourceTest {

    val appId = 1
    val appHash = ""

    @Test
    fun test() {
        Init.init()

        val proxy = TdApi.AddProxy("127.0.0.1", 7890, true, TdApi.ProxyTypeHttp())
        val settings = TDLibSettings.create(APIToken(appId, appHash))
        val clientFactory = SimpleTelegramClientFactory()

        settings.databaseDirectoryPath = Path("src", "test", "resources")
        settings.downloadedFilesDirectoryPath = Path("src", "test", "resources")

        val client = clientFactory.builder(settings).build(AuthenticationSupplier.qrCode())
        client.send(proxy) {}

        client.send(TdApi.GetChatHistory(-1001969491794, 0, 0, 100, false)) {
            val iterator = it.get().messages.iterator()
            while (iterator.hasNext()) {
                val message = iterator.next()
                println("=============>" + message.content)
            }
        }

        // client.sendClose()
        client.waitForExit()
    }

}