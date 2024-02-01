package io.github.shoaky.sourcedownloader.external.bbdown

import io.github.shoaky.sourcedownloader.sdk.http.BaseRequest
import io.github.shoaky.sourcedownloader.sdk.http.HookedApiClient
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class BbDownClient(
    private val endpoint: URI,
) : HookedApiClient() {

    fun <T : Any> execute(request: BaseRequest<T>): HttpResponse<T> {
        return this.execute(endpoint, request)
    }

    override fun <R : BaseRequest<T>, T : Any> beforeRequest(requestBuilder: HttpRequest.Builder, request: R) {

    }

    override fun <R : BaseRequest<T>, T : Any> afterRequest(response: HttpResponse<T>, request: R) {

    }
}