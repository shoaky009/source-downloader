package io.github.shoaky.sourcedownloader.external.fanbox

import com.google.common.net.HttpHeaders
import io.github.shoaky.sourcedownloader.sdk.http.BaseRequest
import io.github.shoaky.sourcedownloader.sdk.http.HookedApiClient
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class FanboxClient(
    private val sessionId: String,
    private val cookie: String? = null,
    private val userAgent: String = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36",
    val server: URI = URI("https://api.fanbox.cc"),
    val headers: Map<String, String> = mapOf(
        // cf_clearance=xxxxxxxxx
        HttpHeaders.COOKIE to "FANBOXSESSID=$sessionId; $cookie",
        HttpHeaders.ORIGIN to "https://www.fanbox.cc",
        HttpHeaders.REFERER to "https://www.fanbox.cc/",
        // "sec-ch-ua" to """Not/A)Brand";v="8", "Chromium";v="126", "Google Chrome";v="126"""",
        "sec-ch-ua-mobile" to "?0",
        "sec-ch-ua-platform" to "\"Windows\"",
        "sec-fetch-site" to "same-site",
        "sec-fetch-mode" to "cors",
        "sec-fetch-dest" to "empty",
        "accept" to "application/json",
        "accept-language" to "en,zh-CN;q=0.9,zh;q=0.8",
        "priority" to "u=1, i",
        HttpHeaders.USER_AGENT to userAgent
    )
) : HookedApiClient() {

    fun <R : BaseRequest<T>, T : Any> execute(request: R): HttpResponse<T> {
        return execute(server, request)
    }

    override fun <R : BaseRequest<T>, T : Any> beforeRequest(requestBuilder: HttpRequest.Builder, request: R) {
        request.setHeaders(headers)
    }

    override fun <R : BaseRequest<T>, T : Any> afterRequest(response: HttpResponse<T>, request: R) {
    }

}