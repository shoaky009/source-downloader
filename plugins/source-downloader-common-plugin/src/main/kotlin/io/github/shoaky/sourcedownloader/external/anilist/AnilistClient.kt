package io.github.shoaky.sourcedownloader.external.anilist

import com.google.common.util.concurrent.RateLimiter
import io.github.shoaky.sourcedownloader.sdk.api.BaseRequest
import io.github.shoaky.sourcedownloader.sdk.api.HookedApiClient
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class AnilistClient(
    private val endpoint: URI = URI("https://graphql.anilist.co"),
    private val autoLimit: Boolean = false
) : HookedApiClient() {

    fun <R : BaseRequest<T>, T : Any> execute(request: R): HttpResponse<T> {
        if (autoLimit) {
            // 随便写 后面再优化
            limiter.acquire()
        }
        return super.execute(endpoint, request)
    }

    override fun <R : BaseRequest<T>, T : Any> beforeRequest(requestBuilder: HttpRequest.Builder, request: R) {
    }

    override fun <R : BaseRequest<T>, T : Any> afterRequest(response: HttpResponse<T>, request: R) {
    }

    companion object {
        private val log = LoggerFactory.getLogger(AnilistClient::class.java)
        private val limiter: RateLimiter = RateLimiter.create(1.0)
    }
}