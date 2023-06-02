package xyz.shoaky.sourcedownloader.telegram.other

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.netty.tcp.TcpClient
import reactor.netty.transport.ProxyProvider
import telegram4j.core.MTProtoTelegramClient
import telegram4j.core.event.DefaultUpdatesManager
import telegram4j.core.retriever.EntityRetrievalStrategy
import telegram4j.core.retriever.PreferredEntityRetriever
import telegram4j.mtproto.MTProtoRetrySpec
import telegram4j.mtproto.MethodPredicate
import telegram4j.mtproto.ResponseTransformer
import telegram4j.mtproto.store.FileStoreLayout
import telegram4j.mtproto.store.StoreLayoutImpl
import telegram4j.tl.json.TlModule
import xyz.shoaky.sourcedownloader.sdk.InstanceFactory
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.telegram.other.auth.QrCodeAuthorization
import java.net.InetSocketAddress
import java.net.URI
import java.nio.file.Path
import java.util.function.Function


object TelegramClientInstanceFactory : InstanceFactory<MTProtoTelegramClient> {
    override fun create(props: Properties): MTProtoTelegramClient {
        val config = props.parse<ClientConfig>()
        val mapper = ObjectMapper().registerModule(TlModule())
        val metadataPath = config.metadataPath.resolve("telegram4j.bin")

        val bootstrap = MTProtoTelegramClient.create(config.apiId, config.apiHash, QrCodeAuthorization::authorize)
        config.proxy?.let { proxy ->
            bootstrap.setTcpClient(TcpClient.newConnection().proxy {
                val builder = it.type(ProxyProvider.Proxy.valueOf(proxy.scheme.uppercase()))
                    .address(InetSocketAddress.createUnresolved(proxy.host, proxy.port))
                proxy.userInfo?.apply {
                    val split = this.split(":")
                    builder.username(split[0]).password { split[1] }
                }
            })
        }
        bootstrap
            .setEntityRetrieverStrategy(
                EntityRetrievalStrategy.preferred(
                    EntityRetrievalStrategy.STORE_FALLBACK_RPC,
                    PreferredEntityRetriever.Setting.FULL,
                    PreferredEntityRetriever.Setting.FULL)
            )
            .setStoreLayout(
                FileStoreLayout(
                    StoreLayoutImpl(Function.identity()),
                    metadataPath
                )
            )
            .addResponseTransformer(
                ResponseTransformer.retryFloodWait(MethodPredicate.all(),
                    MTProtoRetrySpec.max({ it.seconds < 30 }, Long.MAX_VALUE))
            )
            .setUpdatesManager {
                DefaultUpdatesManager(it,
                    DefaultUpdatesManager.Options(DefaultUpdatesManager.Options.DEFAULT_CHECKIN,
                        DefaultUpdatesManager.Options.MAX_USER_CHANNEL_DIFFERENCE,
                        true)
                )
            }
            .withConnection {
                it.mtProtoClientGroup.main().updates().asFlux()
                    .publishOn(Schedulers.boundedElastic())
                    .flatMap { u -> Mono.fromCallable { mapper.writeValueAsString(u) } }
                    .doOnNext(log::info)
                    .then()
            }
        return bootstrap.connect().blockOptional().get()
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
)