package xyz.shoaky.sourcedownloader.external.tmdb

import xyz.shoaky.sourcedownloader.sdk.api.BaseRequest
import xyz.shoaky.sourcedownloader.sdk.api.HookedApiClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TmdbClientV2(
    private val apiKey: String
) : HookedApiClient() {
    override fun <R : BaseRequest<T>, T : Any> beforeRequest(requestBuilder: HttpRequest.Builder, request: R) {

    }

    override fun <R : BaseRequest<T>, T : Any> afterRequest(response: HttpResponse<T>, request: R) {
    }
}