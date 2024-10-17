package io.github.shoaky.sourcedownloader.external.transmission

import com.google.common.net.HttpHeaders
import io.github.shoaky.sourcedownloader.sdk.component.ComponentException
import io.github.shoaky.sourcedownloader.sdk.http.BaseRequest
import io.github.shoaky.sourcedownloader.sdk.http.HookedApiClient
import io.github.shoaky.sourcedownloader.sdk.util.encodeBase64
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TransmissionClient(
    private val endpoint: URI,
    username: String?,
    password: String?,
) : HookedApiClient() {

    private val credentials = "Basic ${"$username:$password".encodeBase64()}}"

    // 不知道会不会过期，先这样写
    private val sessionId: String by lazy {
        val response = execute(endpoint, TestCsrfRequest())
        response.headers().firstValue(SESSION_ID_HEADER).orElseThrow {
            ComponentException.processing("Can't get session id from response headers")
        }
    }

    fun <R : BaseRequest<T>, T : Any> execute(request: R): HttpResponse<T> {
        return super.execute(endpoint, request)
    }

    override fun <R : BaseRequest<T>, T : Any> beforeRequest(requestBuilder: HttpRequest.Builder, request: R) {
        requestBuilder
            .header(HttpHeaders.AUTHORIZATION, credentials)
        if (request is TestCsrfRequest) {
            return
        }
        requestBuilder.header(SESSION_ID_HEADER, sessionId)
    }

    override fun <R : BaseRequest<T>, T : Any> afterRequest(response: HttpResponse<T>, request: R) {
    }

    companion object {
        private const val SESSION_ID_HEADER = "X-Transmission-Session-Id"
    }
}