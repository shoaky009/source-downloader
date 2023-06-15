package io.github.shoaky.sourcedownloader.telegram.auth

import io.github.shoaky.common.Generated
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoSink
import reactor.core.publisher.Sinks
import reactor.core.publisher.Sinks.EmitFailureHandler.FAIL_FAST
import reactor.util.retry.Retry
import telegram4j.core.AuthorizationResources
import telegram4j.mtproto.DataCenter
import telegram4j.mtproto.DcOptions
import telegram4j.mtproto.RpcException
import telegram4j.mtproto.client.MTProtoClientGroup
import telegram4j.mtproto.store.StoreLayout
import telegram4j.tl.ImmutableCodeSettings
import telegram4j.tl.auth.BaseAuthorization
import telegram4j.tl.auth.BaseSentCode
import telegram4j.tl.auth.SentCode
import telegram4j.tl.request.auth.ImmutableCancelCode
import telegram4j.tl.request.auth.ImmutableResendCode
import telegram4j.tl.request.auth.ImmutableSendCode
import telegram4j.tl.request.auth.SignIn
import java.time.Instant
import java.util.*

@Generated
class CodeAuthorization(
    private val clientGroup: MTProtoClientGroup,
    private val storeLayout: StoreLayout,
    private val authResources: AuthorizationResources,
    private val completeSink: MonoSink<BaseAuthorization>?

) {

    private var currentCode: BaseSentCode? = null
    private var phoneNumber: String? = null
    private var firstNumber = true
    private var validPhone = false

    private var state = Sinks.many().replay()
        .latestOrDefault(State.SEND_CODE)
    private val sc: Scanner = Scanner(System.`in`)

    fun begin(): Mono<Void?> {
        return state.asFlux()
            .flatMap { s: State? ->
                when (s) {
                    State.SEND_CODE -> {
                        if (!validPhone) {
                            synchronized(System.out) {
                                println(delimiter)
                                if (!firstNumber) {
                                    print("Invalid phone number, write your phone number again: ")
                                } else {
                                    print("Write your phone number: ")
                                    firstNumber = false
                                }
                            }
                            phoneNumber = readPhoneNumber(sc)
                        }
                        return@flatMap clientGroup.main()
                            .sendAwait<SentCode?>(
                                ImmutableSendCode.of(
                                    phoneNumber!!, authResources.apiId,
                                    authResources.apiHash, ImmutableCodeSettings.of()
                                )
                            )
                            .onErrorResume(RpcException.isErrorMessage("PHONE_NUMBER_INVALID")) {
                                Mono.fromRunnable<SentCode?> {
                                    state.emitNext(
                                        State.SEND_CODE,
                                        FAIL_FAST
                                    )
                                }
                            }
                            .onErrorResume(RpcException.isErrorCode(303)) { e: Throwable ->
                                val rpcExc = e as RpcException
                                val msg = rpcExc.error.errorMessage()
                                if (!msg.startsWith("PHONE_MIGRATE_")) return@onErrorResume Mono.error<SentCode>(
                                    IllegalStateException("Unexpected type of DC redirection", e)
                                )
                                val dcId = msg.substring(14).toInt()
                                validPhone = true
                                synchronized(System.out) {
                                    println(delimiter)
                                    println("Redirecting to the DC $dcId")
                                }
                                storeLayout.dcOptions
                                    .map<DataCenter> { dcOpts: DcOptions ->
                                        dcOpts.find(DataCenter.Type.REGULAR, dcId)
                                            .orElseThrow<IllegalStateException> {
                                                IllegalStateException(
                                                    "Could not find DC " + dcId
                                                        + " for redirecting main client"
                                                )
                                            }
                                    }
                                    .flatMap { clientGroup.setMain(it) }
                                    .then<SentCode?>(Mono.fromRunnable<SentCode?> {
                                        state.emitNext(
                                            State.SEND_CODE,
                                            FAIL_FAST
                                        )
                                    })
                            }
                            .cast<BaseSentCode>(BaseSentCode::class.java)
                            .flatMapMany { scode: BaseSentCode? -> applyCode(scode) }
                    }

                    State.RESEND_CODE -> return@flatMap clientGroup.main()
                        .sendAwait<SentCode>(
                            ImmutableResendCode.of(
                                phoneNumber!!,
                                currentCode!!.phoneCodeHash()
                            )
                        )
                        .cast<BaseSentCode>(BaseSentCode::class.java)
                        .flatMapMany { scode: BaseSentCode? -> applyCode(scode) }

                    State.AWAIT_CODE -> {
                        synchronized(System.out) {
                            println(delimiter)
                            println("Invalid phone code, please write it again")
                        }
                        return@flatMap applyCode(currentCode)
                    }

                    State.CANCEL_CODE -> {
                        println("Goodbye, " + System.getProperty("user.name"))
                        state.emitComplete(FAIL_FAST)
                        completeSink!!.success()
                        return@flatMap Mono.empty<Any>()
                    }

                    State.SIGN_IN -> {
                        state.emitComplete(FAIL_FAST)
                        return@flatMap Mono.empty<Any>()
                    }

                    else -> return@flatMap Flux.error<Any>(IllegalStateException())
                }
            }
            .then()
    }

    enum class State {
        SEND_CODE,
        AWAIT_CODE,
        RESEND_CODE,
        CANCEL_CODE,
        SIGN_IN
    }

    private fun readPhoneNumber(sc: Scanner): String {
        var phoneNumber: String = sc.nextLine()
        if (phoneNumber.startsWith("+")) phoneNumber = phoneNumber.substring(1)
        phoneNumber = phoneNumber.replace(" ".toRegex(), "")
        return phoneNumber
    }

    private fun applyCode(scode: BaseSentCode?): Publisher<*> {
        val resend = currentCode != null
        currentCode = scode
        val sendTimestamp = Instant.now()
        val t = scode!!.timeout()
        synchronized(System.out) {
            println(delimiter)
            val j = StringJoiner(", ")
            j.add("code type: " + scode.type())
            if (t != null) {
                j.add("expires at: " + sendTimestamp.plusSeconds(t.toLong()))
            }
            val nextType = scode.nextType()
            if (nextType != null) {
                j.add("next code type: $nextType")
            }
            println(j)
            print((if (resend) "New code" else "Code") + " has been sent, write it: ")
            j.add((if (resend) "New" else "Sent") + " code: ")
        }
        val code: String = sc.nextLine()
        val expired = t != null && sendTimestamp.plusSeconds(t.toLong()).isAfter(Instant.now())
        if (expired || code.equals("resend", ignoreCase = true)) {
            if (expired) {
                synchronized(System.out) {
                    println(delimiter)
                    println("Code has expired... Sending a new one")
                }
            }
            state.emitNext(State.RESEND_CODE, FAIL_FAST)
            return Mono.empty<Any>()
        }
        return if (code.equals("cancel", ignoreCase = true)) {
            clientGroup.main()
                .sendAwait(ImmutableCancelCode.of(phoneNumber!!, scode.phoneCodeHash()))
                .doOnNext { b: Boolean ->
                    synchronized(System.out) {
                        println(delimiter)
                        if (b) {
                            println("Phone code successfully canceled")
                        } else {
                            println("Failed to cancel phone code")
                        }
                    }
                    state.emitNext(State.CANCEL_CODE, FAIL_FAST)
                }
        } else clientGroup.main()
            .sendAwait(
                SignIn.builder()
                    .phoneNumber(phoneNumber!!)
                    .phoneCode(code)
                    .phoneCodeHash(scode.phoneCodeHash())
                    .build()
            )
            .onErrorResume(RpcException.isErrorMessage("PHONE_CODE_INVALID")) {
                Mono.fromRunnable {
                    state.emitNext(
                        State.AWAIT_CODE,
                        FAIL_FAST
                    )
                }
            }
            .onErrorResume(RpcException.isErrorMessage("SESSION_PASSWORD_NEEDED")) {
                val tfa = TwoFactorAuthHandler(clientGroup)
                tfa.begin2FA().retryWhen(
                    Retry.indefinitely()
                        .filter(RpcException.isErrorMessage("PASSWORD_HASH_INVALID"))
                )
            }
            .cast(BaseAuthorization::class.java)
            .doOnNext { a: BaseAuthorization ->
                synchronized(System.out) {
                    println(delimiter)
                    println("Authorization is successful: $a")
                }
                state.emitNext(State.SIGN_IN, FAIL_FAST)
                completeSink!!.success(a)
            }
    }

    companion object {
        val delimiter = "=".repeat(32)
        fun authorize(
            clientGroup: MTProtoClientGroup, storeLayout: StoreLayout,
            authResources: AuthorizationResources
        ): Mono<BaseAuthorization> {
            return Mono.create { sink: MonoSink<BaseAuthorization> ->
                val instance = CodeAuthorization(clientGroup, storeLayout, authResources, sink)
                sink.onCancel(instance.begin()
                    .subscribe(null) { e: Throwable? -> sink.error(e!!) })
            }
        }
    }
}