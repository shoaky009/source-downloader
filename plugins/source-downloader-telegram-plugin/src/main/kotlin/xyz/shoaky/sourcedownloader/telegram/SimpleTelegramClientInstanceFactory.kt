package xyz.shoaky.sourcedownloader.telegram

import it.tdlight.client.APIToken
import it.tdlight.client.AuthenticationData
import it.tdlight.client.AuthenticationSupplier
import it.tdlight.client.SimpleTelegramClient
import it.tdlight.client.SimpleTelegramClientFactory
import it.tdlight.client.TDLibSettings
import it.tdlight.jni.TdApi
import xyz.shoaky.sourcedownloader.sdk.InstanceFactory
import xyz.shoaky.sourcedownloader.sdk.Properties
import java.net.URI
import java.nio.file.Path
import kotlin.io.path.Path

object SimpleTelegramClientInstanceFactory : InstanceFactory<SimpleTelegramClient> {
    override fun create(props: Properties): SimpleTelegramClient {
        val config = props.parse<ClientConfig>()
        val apiId = config.apiId
        val apiHash = config.apiHash
        // 不清楚这个path需不需要apiId区分
        val databasePath = config.databasePath
        val downloadPath = config.downloadPath

        val settings = TDLibSettings.create(APIToken(apiId, apiHash))

        val clientFactory = SimpleTelegramClientFactory()

        val client = clientFactory.builder(settings)
            .build(AuthenticationSupplier.qrCode())
        settings.databaseDirectoryPath = databasePath
        settings.downloadedFilesDirectoryPath = downloadPath

        val proxyHost = config.proxyHost
        val proxyPort = config.proxyPort
        if (proxyHost != null && proxyPort != null) {
            val type = when (proxyHost.scheme) {
                "http" -> TdApi.ProxyTypeHttp()
                "https" -> TdApi.ProxyTypeHttp()
                "socks5" -> TdApi.ProxyTypeSocks5()
                else -> throw IllegalArgumentException("Unknown proxy type: ${proxyHost.scheme}")
            }
            val proxy = TdApi.AddProxy(proxyHost.host, proxyPort, true, type)
            client.send(proxy) {}
        }

        client.send(TdApi.StopPoll()) {}
        return client
    }

    data class ClientConfig(
        val apiId: Int,
        val apiHash: String,
        val databasePath: Path = Path("telegram"),
        val downloadPath: Path,
        val proxyHost: URI?,
        val proxyPort: Int?,
    )

}