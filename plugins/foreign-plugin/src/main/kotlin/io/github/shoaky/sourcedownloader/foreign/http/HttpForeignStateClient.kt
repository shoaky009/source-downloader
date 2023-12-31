package io.github.shoaky.sourcedownloader.foreign.http

import com.fasterxml.jackson.core.type.TypeReference
import com.google.common.net.HttpHeaders
import com.google.common.net.MediaType
import io.github.shoaky.sourcedownloader.foreign.ForeignStateClient
import io.github.shoaky.sourcedownloader.sdk.util.Jackson
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse

class HttpForeignStateClient(
    private val server: URI,
    private val authorization: String? = null,
    private val client: HttpClient = foreignStateClient
) : ForeignStateClient {

    override fun <T : Any> postState(
        path: String,
        state: Any,
        typeReference: TypeReference<T>
    ): T {
        val request = HttpRequest.newBuilder(server.resolve(path))
            .header(HttpHeaders.AUTHORIZATION, authorization)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString())
            .POST(BodyPublishers.ofString(Jackson.toJsonString(state)))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return Jackson.fromJson(response.body(), typeReference)
    }

    override fun <T : Any> getState(
        path: String,
        typeReference: TypeReference<T>
    ): T {
        val request = HttpRequest.newBuilder(server.resolve(path))
            .header(HttpHeaders.AUTHORIZATION, authorization)
            .GET()
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return Jackson.fromJson(response.body(), typeReference)
    }

    companion object {

        private val foreignStateClient = HttpClient.newBuilder().build()
        private val log = LoggerFactory.getLogger(HttpForeignStateClient::class.java)
    }

}