package io.github.shoaky.sourcedownloader.telegram.other.auth

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import reactor.core.Disposables
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoSink
import reactor.core.scheduler.Schedulers
import reactor.util.Logger
import reactor.util.Loggers
import reactor.util.retry.Retry
import telegram4j.core.AuthorizationResources
import telegram4j.mtproto.DataCenter
import telegram4j.mtproto.DcId
import telegram4j.mtproto.DcOptions
import telegram4j.mtproto.RpcException
import telegram4j.mtproto.client.MTProtoClientGroup
import telegram4j.mtproto.store.StoreLayout
import telegram4j.mtproto.util.CryptoUtil
import telegram4j.mtproto.util.ResettableInterval
import telegram4j.tl.BaseUser
import telegram4j.tl.UpdateLoginToken
import telegram4j.tl.UpdateShort
import telegram4j.tl.api.TlObject
import telegram4j.tl.auth.*
import telegram4j.tl.request.auth.ImmutableExportLoginToken
import telegram4j.tl.request.auth.ImmutableImportLoginToken
import java.time.Duration
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean


object QrCodeAuthorization {

    private val log: Logger = Loggers.getLogger(QrCodeAuthorization::class.java)

    fun authorize(
        clientGroup: MTProtoClientGroup, storeLayout: StoreLayout,
        authResources: AuthorizationResources
    ): Mono<BaseAuthorization> {
        return Mono.create { sink: MonoSink<BaseAuthorization> ->
            val complete = AtomicBoolean()
            val apiId = authResources.apiId
            val apiHash = authResources.apiHash
            val inter = ResettableInterval(Schedulers.boundedElastic())
            val listenTokens = clientGroup.updates().all()
                .takeUntil { complete.get() }
                .ofType(UpdateShort::class.java)
                .filter { u: UpdateShort -> u.update().identifier() == UpdateLoginToken.ID }
                .publishOn(Schedulers.boundedElastic()) // do not block to wait 2FA password
                .flatMap {
                    clientGroup.send(
                        DcId.main(),
                        ImmutableExportLoginToken.of(apiId, apiHash, listOf<Long>())
                    )
                }
                .flatMap { token: LoginToken ->
                    when (token.identifier()) {
                        LoginTokenSuccess.ID -> {
                            val success = token as LoginTokenSuccess
                            return@flatMap Mono.just<Authorization>(success.authorization())
                        }

                        LoginTokenMigrateTo.ID -> {
                            val migrate = token as LoginTokenMigrateTo
                            log.info("Redirecting to the DC {}", migrate.dcId())
                            return@flatMap storeLayout.dcOptions
                                .map<DataCenter> { dcOpts: DcOptions ->
                                    dcOpts.find(DataCenter.Type.REGULAR, migrate.dcId())
                                        .orElseThrow<IllegalStateException> {
                                            IllegalStateException(
                                                "Could not find DC ${migrate.dcId()} for redirecting main client"
                                            )
                                        }
                                }
                                .flatMap { clientGroup.setMain(it) }
                                .flatMap<LoginToken> {
                                    it.sendAwait<LoginToken>(
                                        ImmutableImportLoginToken.of(migrate.token())
                                    )
                                }
                                .cast<LoginTokenSuccess>(LoginTokenSuccess::class.java)
                        }

                        else -> return@flatMap Flux.error<TlObject>(IllegalStateException("Unexpected type of LoginToken: $token"))
                    }
                }
                .onErrorResume(RpcException.isErrorMessage("SESSION_PASSWORD_NEEDED")) {
                    val tfa = TwoFactorAuthHandler(clientGroup)
                    tfa.begin2FA()
                        .retryWhen(
                            Retry.indefinitely()
                                .filter(RpcException.isErrorMessage("PASSWORD_HASH_INVALID"))
                        )
                }
                .cast(LoginTokenSuccess::class.java)
                .doOnNext {
                    val authorization = it.authorization() as BaseAuthorization
                    val b = authorization.user() as BaseUser
                    val j = StringJoiner(" ")
                    Optional.ofNullable(b.firstName()).ifPresent(j::add)
                    Optional.ofNullable(b.lastName()).ifPresent(j::add)
                    val name = j.toString().ifBlank { "unknown" }
                    log.info("Successfully login as {}", name)
                    complete.set(true)
                    inter.dispose()
                    sink.success(authorization)
                }
                .subscribe()
            inter.start(Duration.ofMinutes(1)) // stub period
            val qrDisplay = inter.ticks()
                .flatMap {
                    clientGroup.send(
                        DcId.main(),
                        ImmutableExportLoginToken.of(apiId, apiHash, listOf<Long>())
                    )
                }
                .cast(BaseLoginToken::class.java)
                .doOnNext {
                    val duration = Duration.ofSeconds(it.expires() - System.currentTimeMillis() / 1000)
                    val token = Base64.getUrlEncoder().encodeToString(CryptoUtil.toByteArray(it.token()))
                    val url = "tg://login?token=$token"
                    synchronized(System.out) {
                        println("QR code (you have " + duration.seconds + " seconds):")
                        println(generateQr(url))
                        println()
                    }
                    inter.start(duration, duration)
                }
                .subscribe()
            sink.onCancel(Disposables.composite(listenTokens, qrDisplay))
        }
    }

    private fun generateQr(text: String): String? {
        val width = 40
        val height = 40
        val qrParam: Hashtable<EncodeHintType, Any> = Hashtable()
        qrParam[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.L
        qrParam[EncodeHintType.CHARACTER_SET] = "utf-8"
        return try {
            val bitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height, qrParam)
            toAscii(bitMatrix)
        } catch (ex: WriterException) {
            throw java.lang.IllegalStateException("Can't encode QR code", ex)
        }
    }

    private fun toAscii(bitMatrix: BitMatrix): String {
        val sb = StringBuilder()
        for (rows in 0 until bitMatrix.height) {
            for (cols in 0 until bitMatrix.width) {
                val x = bitMatrix[rows, cols]
                sb.append(if (x) "  " else "██")
            }
            sb.append("\n")
        }
        return sb.toString()
    }
}