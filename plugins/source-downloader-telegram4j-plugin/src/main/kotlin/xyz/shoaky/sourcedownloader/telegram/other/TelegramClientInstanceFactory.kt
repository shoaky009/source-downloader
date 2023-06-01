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
import java.net.InetSocketAddress
import java.net.URI
import java.nio.file.Path
import java.util.function.Function


object TelegramClientInstanceFactory : InstanceFactory<MTProtoTelegramClient> {
    override fun create(props: Properties): MTProtoTelegramClient {
        val config = props.parse<ClientConfig>()
        val mapper = ObjectMapper().registerModule(TlModule())
        val metadataPath = config.metadataPath.resolve("telegram4j.bin")

        val bootstrap = MTProtoTelegramClient.create(config.apiId, config.apiHash, CodeAuthorization::authorize)
        config.proxy?.let { proxy ->
            bootstrap.setTcpClient(TcpClient.newConnection().proxy {
                it.type(ProxyProvider.Proxy.valueOf(proxy.scheme.uppercase()))
                    .address(InetSocketAddress.createUnresolved(proxy.host, proxy.port))
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
                    // Path.of("plugins/source-downloader-telegram-plugin/src/test/resources/t4j.bin")
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
        bootstrap.withConnection {
            it.mtProtoClientGroup.main().updates().asFlux()
                .publishOn(Schedulers.boundedElastic())
                .flatMap { u -> Mono.fromCallable { mapper.writeValueAsString(u) } }
                .doOnNext(log::info)
                .then()
        }.subscribe()
        return bootstrap.connect().blockOptional().get()
    }

    private val log = LoggerFactory.getLogger("Telegram4j")
}

private data class ClientConfig(
    @JsonAlias("api-id")
    val apiId: Int,
    @JsonAlias("api-hash")
    val apiHash: String,
    @JsonAlias("metadata-path")
    val metadataPath: Path,
    val proxy: URI?,
)