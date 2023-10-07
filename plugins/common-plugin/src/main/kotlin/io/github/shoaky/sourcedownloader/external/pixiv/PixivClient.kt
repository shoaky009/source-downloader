package io.github.shoaky.sourcedownloader.external.pixiv

import com.google.common.net.HttpHeaders
import io.github.shoaky.sourcedownloader.sdk.api.BaseRequest
import io.github.shoaky.sourcedownloader.sdk.api.HookedApiClient
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class PixivClient(
    private val sessionId: String? = null
) : HookedApiClient() {

    private val server: URI = URI("https://www.pixiv.net")
    val basicHeaders =
        buildMap {
            sessionId?.let {
                this[HttpHeaders.COOKIE] = "PHPSESSID=$sessionId; "
            }
            this[HttpHeaders.REFERER] = "https://www.pixiv.net/"
            this[HttpHeaders.USER_AGENT] = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36"
            this["sec-ch-ua"] = """"Google Chrome";v="117", "Not;A=Brand";v="8", "Chromium";v="117""""
            this["sec-ch-ua-mobile"] = "?0"
            this["sec-ch-ua-platform"] = "\"Windows\""
            this["sec-fetch-dest"] = "empty"
            this["sec-fetch-mode"] = "cors"
            this["sec-fetch-site"] = "same-origin"
        }

    fun <T : Any> execute(request: BaseRequest<T>): HttpResponse<T> {
        return this.execute(server, request)
    }

    override fun <R : BaseRequest<T>, T : Any> beforeRequest(requestBuilder: HttpRequest.Builder, request: R) {
        request.setHeaders(basicHeaders)
    }

    override fun <R : BaseRequest<T>, T : Any> afterRequest(response: HttpResponse<T>, request: R) {
    }
}