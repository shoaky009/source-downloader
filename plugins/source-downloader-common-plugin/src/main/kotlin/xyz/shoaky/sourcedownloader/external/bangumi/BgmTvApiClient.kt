package xyz.shoaky.sourcedownloader.external.bangumi

import org.springframework.http.HttpHeaders
import xyz.shoaky.sourcedownloader.sdk.api.BaseRequest
import xyz.shoaky.sourcedownloader.sdk.api.HookedApiClient
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class BgmTvApiClient(
    private var token: String? = null,
    private val endpoint: URI = URI("https://api.bgm.tv/")
) : HookedApiClient() {

    fun <R : BaseRequest<T>, T : Any> execute(request: R): HttpResponse<T> {
        return super.execute(endpoint, request)
    }

    override fun <R : BaseRequest<T>, T : Any> beforeRequest(requestBuilder: HttpRequest.Builder, request: R) {
        if (token != null) {
            requestBuilder.header(HttpHeaders.AUTHORIZATION, "Bearer $token")
        }
    }

    override fun <R : BaseRequest<T>, T : Any> afterRequest(response: HttpResponse<T>, request: R) {
    }
}