package io.github.shoaky.sourcedownloader.external.anilist

import com.google.common.util.concurrent.RateLimiter
import io.github.shoaky.sourcedownloader.sdk.http.BaseRequest
import io.github.shoaky.sourcedownloader.sdk.http.HookedApiClient
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

@Suppress("UnstableApiUsage")
class AnilistClient(
    private val endpoint: URI = URI("https://graphql.anilist.co"),
) : HookedApiClient() {

    fun <R : BaseRequest<T>, T : Any> execute(request: R): HttpResponse<T> {
        val response = super.execute(endpoint, request)
        limiter.acquire()
        log.debug("Rate limit remaining: {}", response.headers().firstValueAsLong("x-ratelimit-remaining").orElse(-1))
        if (response.statusCode() == 429) {
            val retryAfter = response.headers().firstValueAsLong("retry-after").orElse(5L)
            val reset = response.headers().firstValueAsLong("x-ratelimit-reset")
            log.warn("Rate limit exceeded, Waiting for $retryAfter seconds, reset at $reset")
            Thread.sleep(Duration.ofSeconds(retryAfter))
            return execute(request)
        }
        return response
    }

    override fun <R : BaseRequest<T>, T : Any> beforeRequest(requestBuilder: HttpRequest.Builder, request: R) {
    }

    override fun <R : BaseRequest<T>, T : Any> afterRequest(response: HttpResponse<T>, request: R) {
    }

    companion object {

        private val limiter: RateLimiter = RateLimiter.create(0.7)
        private val log = LoggerFactory.getLogger(AnilistClient::class.java)
    }

}