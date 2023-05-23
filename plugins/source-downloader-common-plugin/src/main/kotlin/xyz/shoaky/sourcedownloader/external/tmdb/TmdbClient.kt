package xyz.shoaky.sourcedownloader.external.tmdb

import xyz.shoaky.sourcedownloader.sdk.api.BaseRequest
import xyz.shoaky.sourcedownloader.sdk.api.HookedApiClient
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TmdbClient(
    private val apiKey: String,
    private val uri: URI = URI.create("https://api.themoviedb.org")
) : HookedApiClient() {

    fun <R : BaseRequest<T>, T : Any> execute(request: R): HttpResponse<T> {
        return super.execute(uri, request)
    }

    override fun <R : BaseRequest<T>, T : Any> beforeRequest(requestBuilder: HttpRequest.Builder, request: R) {
        request.queryString["api_key"] = apiKey
        // requestBuilder.header(HttpHeaders.AUTHORIZATION, "Bearer $apiKey")
    }

    override fun <R : BaseRequest<T>, T : Any> afterRequest(response: HttpResponse<T>, request: R) {
    }

    companion object {
        const val DEFAULT_TOKEN = "7d82a6a830d5f4458f42929f73878195"
    }
}