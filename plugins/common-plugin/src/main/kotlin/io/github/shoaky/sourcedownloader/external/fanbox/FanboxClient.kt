package io.github.shoaky.sourcedownloader.external.fanbox

import com.google.common.net.HttpHeaders
import io.github.shoaky.sourcedownloader.sdk.http.BaseRequest
import io.github.shoaky.sourcedownloader.sdk.http.HookedApiClient
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class FanboxClient(
    private val cookie: String,
    private val userAgent: String = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0",
    val server: URI = URI("https://api.fanbox.cc"),
    val headers: Map<String, String> = mapOf(
        HttpHeaders.COOKIE to cookie,
        HttpHeaders.ORIGIN to "https://www.fanbox.cc",
        HttpHeaders.REFERER to "https://www.fanbox.cc/",
        HttpHeaders.ACCEPT to "application/json, text/plain, */*",
        "sec-ch-ua" to """"Not:A-Brand";v="99", "Microsoft Edge";v="145", "Chromium";v="145"""",
        "sec-ch-ua-mobile" to "?0",
        "sec-ch-ua-platform" to "\"Windows\"",
        "sec-fetch-site" to "same-site",
        "sec-fetch-mode" to "cors",
        "sec-fetch-dest" to "empty",
        "accept" to "application/json",
        "accept-language" to "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6",
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