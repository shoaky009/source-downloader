package io.github.shoaky.sourcedownloader.sdk.util.http

import org.slf4j.LoggerFactory
import java.net.CookieManager
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.time.Duration
import java.util.concurrent.Executors

val defaultCookieManager = CookieManager()
val httpClient: HttpClient = HttpClient.newBuilder()
    .executor(Executors.newVirtualThreadPerTaskExecutor())
    .cookieHandler(defaultCookieManager)
    .connectTimeout(Duration.ofSeconds(10))
    .build()

internal val log = LoggerFactory.getLogger("HTTP")

fun httpGetRequest(
    uri: URI,
    headers: Map<String, String> = emptyMap()
): HttpRequest {
    val builder = HttpRequest.newBuilder(uri).GET()
    headers.forEach(builder::header)
    return builder.build()
}