package io.github.shoaky.sourcedownloader.external.fanbox

import io.github.shoaky.sourcedownloader.sdk.api.BaseRequest
import io.github.shoaky.sourcedownloader.sdk.api.HookedApiClient
import org.springframework.http.HttpHeaders
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class FanboxClient(
    private val sessionId: String,
    val server: URI = URI("https://api.fanbox.cc"),
    val headers: Map<String, String> = mapOf(
        HttpHeaders.COOKIE to "FANBOXSESSID=$sessionId",
        HttpHeaders.ORIGIN to "https://www.fanbox.cc",
        HttpHeaders.REFERER to "https://www.fanbox.cc/",
        "sec-ch-ua" to """Not.A/Brand";v="8", "Chromium";v="114", "Google Chrome";v="114"""",
        "sec-ch-ua-mobile" to "?0",
        "sec-ch-ua-platform" to "Windows",
        HttpHeaders.USER_AGENT to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36"
    )
) : HookedApiClient() {

    fun <R : BaseRequest<T>, T : Any> execute(request: R): HttpResponse<T> {
        return execute(server, request)
    }

    override fun <R : BaseRequest<T>, T : Any> beforeRequest(requestBuilder: HttpRequest.Builder, request: R) {
        request.setHeaders(
            headers
        )
    }

    override fun <R : BaseRequest<T>, T : Any> afterRequest(response: HttpResponse<T>, request: R) {
    }

}