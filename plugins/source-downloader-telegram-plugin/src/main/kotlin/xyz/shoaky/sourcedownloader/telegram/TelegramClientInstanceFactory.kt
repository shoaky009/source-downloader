package io.github.shoaky.sourcedownloader.telegram

import com.fasterxml.jackson.annotation.JsonAlias
import it.tdlight.client.*
import it.tdlight.jni.TdApi
import io.github.shoaky.sourcedownloader.sdk.InstanceFactory
import io.github.shoaky.sourcedownloader.sdk.Properties
import java.net.URI
import java.nio.file.Path

class TelegramClientInstanceFactory(
    private val applicationDataPath: Path
) : InstanceFactory<SimpleTelegramClient> {

    override fun create(props: Properties): SimpleTelegramClient {
        val config = props.parse<ClientConfig>()

        val settings = TDLibSettings.create(APIToken(config.apiId, config.apiHash))
        val clientFactory = SimpleTelegramClientFactory()
        val client = clientFactory.builder(settings)
            .build(AuthenticationSupplier.qrCode())
        // 不清楚这个path需不需要apiId区分
        if (config.metadataPath.isAbsolute) {
            settings.databaseDirectoryPath = config.metadataPath
        } else {
            settings.databaseDirectoryPath = applicationDataPath.resolve(config.metadataPath)
        }
        settings.downloadedFilesDirectoryPath = config.downloadPath

        val proxy = config.proxy
        if (proxy != null) {
            val type = when (proxy.scheme) {
                "http" -> TdApi.ProxyTypeHttp()
                "https" -> TdApi.ProxyTypeHttp()
                "socks5" -> TdApi.ProxyTypeSocks5()
                else -> throw IllegalArgumentException("Unknown proxy type: ${proxy.scheme}")
            }
            client.send(TdApi.AddProxy(proxy.host, proxy.port, true, type)) {}
        }
        return client
    }

    override fun type(): Class<SimpleTelegramClient> {
        return SimpleTelegramClient::class.java
    }

    data class ClientConfig(
        @JsonAlias("api-id")
        val apiId: Int,
        @JsonAlias("api-hash")
        val apiHash: String,
        @JsonAlias("metadata-path")
        val metadataPath: Path,
        @JsonAlias("download-path")
        val downloadPath: Path,
        val proxy: URI?,
    )

}