package io.github.shoaky.sourcedownloader.external.chii

import io.github.shoaky.sourcedownloader.sdk.http.BaseRequest
import io.github.shoaky.sourcedownloader.sdk.http.HookedApiClient
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class ChiiClient(
    private val endpoint: URI = URI("https://chii.ai"),
) : HookedApiClient() {

    fun <R : BaseRequest<T>, T : Any> execute(request: R): HttpResponse<T> {
        return super.execute(endpoint, request)
    }

    override fun <R : BaseRequest<T>, T : Any> beforeRequest(requestBuilder: HttpRequest.Builder, request: R) {
    }

    override fun <R : BaseRequest<T>, T : Any> afterRequest(response: HttpResponse<T>, request: R) {
    }
}