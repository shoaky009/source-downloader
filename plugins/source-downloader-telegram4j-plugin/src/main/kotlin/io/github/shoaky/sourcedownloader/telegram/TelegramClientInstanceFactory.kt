package io.github.shoaky.sourcedownloader.telegram

import com.fasterxml.jackson.annotation.JsonAlias
import io.github.shoaky.sourcedownloader.sdk.InstanceFactory
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.telegram.auth.QrCodeAuthorization
import io.netty.util.ResourceLeakDetector
import org.slf4j.LoggerFactory
import reactor.core.publisher.Hooks
import telegram4j.core.MTProtoTelegramClient
import telegram4j.core.event.DefaultUpdatesManager
import telegram4j.core.retriever.EntityRetrievalStrategy
import telegram4j.core.retriever.PreferredEntityRetriever
import telegram4j.mtproto.MTProtoRetrySpec
import telegram4j.mtproto.MethodPredicate
import telegram4j.mtproto.ResponseTransformer
import telegram4j.mtproto.resource.EventLoopResources
import telegram4j.mtproto.resource.ProxyResources
import telegram4j.mtproto.resource.TcpClientResources
import telegram4j.mtproto.store.FileStoreLayout
import telegram4j.mtproto.store.StoreLayoutImpl
import java.net.InetSocketAddress
import java.net.URI
import java.nio.file.Path
import java.time.Duration
import java.util.*
import java.util.function.Function
import kotlin.io.path.createDirectories

object TelegramClientInstanceFactory : InstanceFactory<MTProtoTelegramClient> {
    override fun create(props: Properties): MTProtoTelegramClient {
        val config = props.parse<ClientConfig>()
        val metadataPath = config.metadataPath.resolve("telegram4j.bin")
        config.metadataPath.createDirectories()

        val bootstrap = MTProtoTelegramClient.create(
            config.apiId,
            config.apiHash,
            QrCodeAuthorization::authorize
        )

        val proxyResource = props.getOrNull<URI>("proxy")?.let {
            val address = InetSocketAddress(it.host, it.port)
            when (it.scheme) {
                "http" -> ProxyResources.ofHttp().address(address).build()
                "socks5" -> ProxyResources.ofSocks5().address(address).build()
                else -> throw IllegalArgumentException("unsupported proxy type ${it.scheme}")
            }
        }
        proxyResource?.apply {
            bootstrap.setTcpClientResources(
                TcpClientResources.builder()
                    .eventLoopResources(EventLoopResources.create())
                    .proxyResources(this).build()
            )
        }

        bootstrap
            .setEntityRetrieverStrategy(
                EntityRetrievalStrategy.preferred(
                    EntityRetrievalStrategy.STORE_FALLBACK_RPC,
                    PreferredEntityRetriever.Setting.FULL,
                    PreferredEntityRetriever.Setting.FULL
                )
            )
            .setStoreLayout(
                FileStoreLayout(
                    StoreLayoutImpl(Function.identity()),
                    metadataPath
                )
            )
            .addResponseTransformer(
                ResponseTransformer.retryFloodWait(
                    MethodPredicate.all(),
                    MTProtoRetrySpec.max({ it.seconds < 30 }, 2)
                )
            )
            .setUpdatesManager {
                DefaultUpdatesManager(
                    it,
                    DefaultUpdatesManager.Options(
                        DefaultUpdatesManager.Options.DEFAULT_CHECKIN,
                        DefaultUpdatesManager.Options.MAX_USER_CHANNEL_DIFFERENCE,
                        true
                    )
                )
            }

        if (config.debug) {
            Hooks.onOperatorDebug()
            ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID)
        }

        return bootstrap.connect()
            .doOnError {
                log.error("Error while connecting to Telegram", it)
            }
            .blockOptional(Duration.ofSeconds(30)).get()
    }

    override fun type(): Class<MTProtoTelegramClient> {
        return MTProtoTelegramClient::class.java
    }

}

internal val log = LoggerFactory.getLogger("Telegram4j")

private data class ClientConfig(
    @JsonAlias("api-id")
    val apiId: Int,
    @JsonAlias("api-hash")
    val apiHash: String,
    @JsonAlias("metadata-path")
    val metadataPath: Path,
    val proxy: URI?,
    val debug: Boolean = false
)