package io.github.shoaky.sourcedownloader.external.patreon

import com.google.common.net.HttpHeaders
import io.github.shoaky.sourcedownloader.sdk.api.BaseRequest
import io.github.shoaky.sourcedownloader.sdk.api.HookedApiClient
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class PatreonClient(
    private val sessionId: String,
    private val server: URI = URI("https://www.patreon.com")
) : HookedApiClient() {

    val basicHeaders = mapOf(
        HttpHeaders.ACCEPT_LANGUAGE to "en,zh-CN;q=0.9,zh;q=0.8",
        HttpHeaders.USER_AGENT to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36",
        HttpHeaders.COOKIE to "session_id=$sessionId; patreon_location_country_code=CN; patreon_locale_code=zh-CN;"
    )

    fun <R : BaseRequest<T>, T : Any> execute(request: R): HttpResponse<T> {
        return execute(server, request)
    }

    override fun <R : BaseRequest<T>, T : Any> beforeRequest(requestBuilder: HttpRequest.Builder, request: R) {
        request.setHeaders(basicHeaders)
    }

    override fun <R : BaseRequest<T>, T : Any> afterRequest(response: HttpResponse<T>, request: R) {

    }

}