package xyz.shoaky.sourcedownloader.telegram

import it.tdlight.client.APIToken
import it.tdlight.client.AuthenticationData
import it.tdlight.client.SimpleTelegramClient
import it.tdlight.client.TDLibSettings
import it.tdlight.common.Init
import it.tdlight.jni.TdApi
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

class TelegramSourceTest {

    @Test
    fun name() {
        Init.start()

        val appId = 1
        val appHash = ""

        val proxy = TdApi.AddProxy("127.0.0.1", 7890, true, TdApi.ProxyTypeHttp())
        val settings = TDLibSettings.create(APIToken(appId, appHash))

        val client = SimpleTelegramClient(settings)
        settings.databaseDirectoryPath = Path("src/test/resources")
        val qrCode = AuthenticationData.qrCode()

        client.start(qrCode)
        client.send(proxy) {
            println(it.get())
        }
        client.send(TdApi.StopPoll()) {}

        client.send(TdApi.GetChatHistory(-1001969491794, 0, 0, 0, false)) {
            val iterator = it.get().messages.iterator()
            while (iterator.hasNext()) {
                val message = iterator.next()
                println(message.content)
            }
        }

            client.waitForExit()
    }
}