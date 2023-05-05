package xyz.shoaky.sourcedownloader.ai

import xyz.shoaky.sourcedownloader.sdk.api.BaseRequest
import xyz.shoaky.sourcedownloader.sdk.api.HookedApiClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class OpenAiClient(
    private val apiKeys: List<String>,
    private val timeout: Duration = Duration.ofSeconds(10L)
) : HookedApiClient() {
    override fun <R : BaseRequest<T>, T : Any> beforeRequest(requestBuilder: HttpRequest.Builder, request: R) {
        requestBuilder.header("Authorization", "Bearer ${apiKeys.random()}")
        requestBuilder.timeout(timeout)
    }

    override fun <R : BaseRequest<T>, T : Any> afterRequest(response: HttpResponse<T>, request: R) {

    }
}