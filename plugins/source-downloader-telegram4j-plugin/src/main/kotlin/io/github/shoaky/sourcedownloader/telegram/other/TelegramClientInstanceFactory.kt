package io.github.shoaky.sourcedownloader.telegram.other

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import io.github.shoaky.sourcedownloader.sdk.InstanceFactory
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.telegram.other.auth.QrCodeAuthorization
import io.netty.util.ResourceLeakDetector
import org.slf4j.LoggerFactory
import reactor.core.publisher.Hooks
import telegram4j.core.InitConnectionParams
import telegram4j.core.MTProtoTelegramClient
import telegram4j.core.event.DefaultUpdatesManager
import telegram4j.core.retriever.EntityRetrievalStrategy
import telegram4j.core.retriever.PreferredEntityRetriever
import telegram4j.mtproto.*
import telegram4j.mtproto.store.FileStoreLayout
import telegram4j.mtproto.store.StoreLayoutImpl
import telegram4j.tl.InputClientProxy
import java.lang.module.ModuleDescriptor
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
        val initConnectionParams = initConnectionParams(props.getOrNull<URI>("proxy"))
        bootstrap.setInitConnectionParams(initConnectionParams)
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
            .blockOptional(Duration.ofSeconds(10)).get()
    }

    private fun initConnectionParams(proxyUri: URI?): InitConnectionParams {
        val appVersion = Optional.ofNullable(
            InitConnectionParams::class.java.module.descriptor
        )
            .flatMap { obj: ModuleDescriptor -> obj.rawVersion() }
            .orElse("0.1.0-SNAPSHOT")
        val deviceModel = "Telegram4J"
        val systemVersion = java.lang.String.join(
            " ", System.getProperty("os.name"),
            System.getProperty("os.version"),
            System.getProperty("os.arch")
        )

        val langCode = Locale.getDefault().language.lowercase()
        val node: JsonNode = JsonNodeFactory.instance.objectNode()
            .put("tz_offset", TimeZone.getDefault().rawOffset / 1000.0)

        val proxy = proxyUri?.let {
            InputClientProxy.builder()
                .port(it.port)
                .address(it.host)
                .build()
        }
        return InitConnectionParams(
            appVersion, deviceModel, langCode,
            "", systemVersion, langCode, proxy, node
        )
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