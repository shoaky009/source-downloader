package io.github.shoaky.sourcedownloader.telegram

import com.fasterxml.jackson.annotation.JsonAlias
import io.github.shoaky.sourcedownloader.sdk.InstanceFactory
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.Sleepable
import io.github.shoaky.sourcedownloader.telegram.auth.QRCallback
import io.netty.util.ResourceLeakDetector
import org.slf4j.LoggerFactory
import reactor.core.publisher.Hooks
import telegram4j.core.MTProtoBootstrap
import telegram4j.core.MTProtoTelegramClient
import telegram4j.core.auth.QRAuthorizationHandler
import telegram4j.core.retriever.EntityRetrievalStrategy
import telegram4j.core.retriever.PreferredEntityRetriever
import telegram4j.mtproto.client.ReconnectionStrategy
import telegram4j.mtproto.resource.EventLoopResources
import telegram4j.mtproto.resource.ProxyResources
import telegram4j.mtproto.resource.TcpClientResources
import telegram4j.mtproto.store.FileStoreLayout
import telegram4j.mtproto.store.StoreLayoutImpl
import java.net.InetSocketAddress
import java.net.URI
import java.nio.file.Path
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantLock
import java.util.function.Function
import kotlin.concurrent.withLock
import kotlin.io.path.createDirectories

object TelegramClientInstanceFactory : InstanceFactory<TelegramClientWrapper> {

    override fun create(props: Properties): TelegramClientWrapper {
        val config = props.parse<ClientConfig>()
        config.metadataPath.createDirectories()

        val bootstrap = newBootstrap(config)
        // just check bootstrap
        val client = bootstrap.connect()
            .doOnError {
                log.error("Error while connecting to Telegram", it)
            }
            .blockOptional(Duration.ofSeconds(config.timeout)).get()
        client.disconnect().subscribe()
        return TelegramClientWrapper(config)
    }

    internal fun newBootstrap(config: ClientConfig): MTProtoBootstrap {
        val metadataPath = config.metadataPath.resolve("telegram4j.bin")
        val bootstrap = MTProtoTelegramClient.create(
            config.apiId,
            config.apiHash,
            QRAuthorizationHandler(QRCallback)
        )
        bootstrap.setResultPublisher(Executors.newVirtualThreadPerTaskExecutor())

        val proxyResource = config.proxy?.let {
            val address = InetSocketAddress(it.host, it.port)
            when (it.scheme) {
                "http" -> ProxyResources.ofHttp().address(address).connectTimeout(Duration.ofSeconds(5)).build()
                "socks5" -> ProxyResources.ofSocks5().address(address).connectTimeout(Duration.ofSeconds(5)).build()
                else -> throw IllegalArgumentException("Unsupported proxy type ${it.scheme}")
            }
        }
        proxyResource?.apply {
            bootstrap.setTcpClientResources(
                TcpClientResources.builder()
                    .eventLoopResources(EventLoopResources.create())
                    .proxyResources(this).build()
            )
        }

        val reconnectionStrategy = if (config.reconnectionInterval < 1L) {
            ReconnectionStrategy.immediately()
        } else {
            ReconnectionStrategy.fixedInterval(
                Duration.ofSeconds(config.reconnectionInterval)
            )
        }

        bootstrap
            .setReconnectionStrategy(reconnectionStrategy)
            .setPingInterval(Duration.ofSeconds(config.pingInterval))
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

        if (config.debug) {
            Hooks.onOperatorDebug()
            ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID)
        }
        return bootstrap
    }

    override fun type(): Class<TelegramClientWrapper> {
        return TelegramClientWrapper::class.java
    }

}

class TelegramClientWrapper(
    private val config: ClientConfig,
) : AutoCloseable, Sleepable {

    private var client: MTProtoTelegramClient? = null

    // 暂时不考虑多线程
    private val sources: MutableSet<String> = mutableSetOf()
    private val lock = ReentrantLock()
    override fun close() {
        sources.clear()
        client?.disconnect()?.block(Duration.ofSeconds(3L))
        log.info("Telegram client closed")
    }

    private fun wakeUp() {
        lock.withLock {
            if (client != null) {
                return@withLock
            }
            val bootstrap = TelegramClientInstanceFactory.newBootstrap(config)
            client = bootstrap.connect()
                .doOnError {
                    log.error("Error while connecting to Telegram", it)
                }
                //
                .blockOptional(Duration.ofSeconds(5L))
                .get()
        }
    }

    override fun inUse(): Boolean {
        return sources.isNotEmpty()
    }

    override fun use(source: String) {
        val inUse = inUse()
        if (inUse.not()) {
            log.info("Not in use starting to connect")
            wakeUp()
        }
        sources.add(source)
    }

    override fun release(source: String) {
        sources.remove(source)
        if (sources.isEmpty()) {
            log.info("No source in use, closing Telegram client")
            client?.disconnect()?.block(Duration.ofSeconds(3L))
            client = null
        }
    }

    fun getClient(): MTProtoTelegramClient {
        return client ?: throw IllegalStateException("Not in use, call use(source) first")
    }

    companion object {

        private val log = LoggerFactory.getLogger(TelegramClientWrapper::class.java)
    }

}

internal val log = LoggerFactory.getLogger("Telegram4j")

data class ClientConfig(
    @JsonAlias("api-id")
    val apiId: Int,
    @JsonAlias("api-hash")
    val apiHash: String,
    @JsonAlias("metadata-path")
    val metadataPath: Path,
    val proxy: URI?,
    val debug: Boolean = false,
    @JsonAlias("ping-interval")
    val pingInterval: Long = 30L,
    @JsonAlias("reconnection-interval")
    val reconnectionInterval: Long = 15L,
    val timeout: Long = 5L,
)