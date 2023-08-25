package io.github.shoaky.sourcedownloader.sdk.util.http

import org.slf4j.LoggerFactory
import java.net.CookieManager
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest

val defaultCookieManager = CookieManager()
val httpClient: HttpClient = HttpClient.newBuilder()
    .cookieHandler(defaultCookieManager)
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

interface BodyMapper<T : Any> {

    fun mapping(info: MappingInfo<T>): T
}