package xyz.shoaky.sourcedownloader.sdk.api.bangumi

import xyz.shoaky.sourcedownloader.sdk.api.BaseRequest
import xyz.shoaky.sourcedownloader.sdk.api.HookedApiClient
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse

object BangumiApiClient : HookedApiClient() {

    private val endpoint by lazy {
        val s = System.getenv("BANGUMI_API_SERVER") ?: "https://api.bgm.tv/"
        URI(s) }
    fun <R : BaseRequest<T>, T : Any> execute(request: R): HttpResponse<T> {
        return super.execute(endpoint, request)
    }

    override fun <R : BaseRequest<T>, T : Any> beforeRequest(requestBuilder: HttpRequest.Builder, request: R) {

    }

    override fun <R : BaseRequest<T>, T : Any> afterRequest(response: HttpResponse<T>, request: R) {
    }
}