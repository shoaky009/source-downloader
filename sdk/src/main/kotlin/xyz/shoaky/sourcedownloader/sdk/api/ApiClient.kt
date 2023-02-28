package xyz.shoaky.sourcedownloader.sdk.api

import com.fasterxml.jackson.core.type.TypeReference
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.web.util.UriComponentsBuilder
import xyz.shoaky.sourcedownloader.sdk.api.BaseRequest.Companion.stringTypeReference
import xyz.shoaky.sourcedownloader.sdk.util.Jackson
import java.io.InputStream
import java.net.CookieManager
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

interface ApiClient {

    fun <R : BaseRequest<T>, T : Any> execute(endpoint: URI, request: R): HttpResponse<T>

}

abstract class HookedApiClient : ApiClient {

    override fun <R : BaseRequest<T>, T : Any> execute(endpoint: URI, request: R): HttpResponse<T> {
        val uriBuilder = UriComponentsBuilder.fromUri(endpoint).path(request.path)

        if (request.httpMethod === HttpMethod.GET) {
            Jackson.convertToMap(this)
                .forEach {
                    uriBuilder.queryParam(it.key, it.value)
                }
        }
        request.queryString.forEach { (k, v) ->
            uriBuilder.queryParam(k, v)
        }

        val requestBuilder = HttpRequest.newBuilder(uriBuilder.build().toUri())
            .method(request.httpMethod.name(), bodyPublisher(request))
        request.httpHeaders.set(HttpHeaders.CONTENT_TYPE, request.mediaType.toString())
        request.httpHeaders.forEach { (name, value) -> requestBuilder.header(name, value.joinToString { it }) }

        beforeRequest(requestBuilder, request)
        val httpRequest = requestBuilder.build()
        val httpResponse = httpClient.send(httpRequest, bodyHandler(request))
        if (log.isDebugEnabled) {
            log.debug(
                """
                request:{}
                requestBody:{}
                responseCode:{}
                responseBody:{}
                """, httpRequest.uri(), Jackson.toJsonString(httpRequest),
                httpResponse.statusCode(), httpResponse.body())
        }
        afterRequest(httpResponse, request)
        return httpResponse as HttpResponse<T>
    }

    abstract fun <R : BaseRequest<T>, T : Any> beforeRequest(requestBuilder: HttpRequest.Builder, request: R)

    abstract fun <R : BaseRequest<T>, T : Any> afterRequest(response: HttpResponse<T>, request: R)

    private fun <R : BaseRequest<T>, T : Any> bodyHandler(request: BaseRequest<T>): HttpResponse.BodyHandler<T> {
        if (request.mediaType == MediaType.APPLICATION_JSON) {
            return JsonBodyHandler(request.responseBodyType)
        }
        if (request.responseBodyType == stringTypeReference) {
            @Suppress("UNCHECKED_CAST")
            return stringBodyHandler as HttpResponse.BodyHandler<T>
        }
        return JsonBodyHandler(request.responseBodyType)
    }

    private fun <R : BaseRequest<T>, T : Any> bodyPublisher(request: BaseRequest<T>): HttpRequest.BodyPublisher {
        val mediaType = request.mediaType
        if (mediaType == MediaType.APPLICATION_FORM_URLENCODED || mediaType == MediaType.TEXT_PLAIN) {
            val data = Jackson.convert(request, object : TypeReference<Map<String, Any?>>() {})
            val query = data.entries.stream()
                .filter { (_, v) -> v != null }
                .map { (k, v) ->
                    "${URLEncoder.encode(k, Charsets.UTF_8)}=${URLEncoder.encode(v.toString(), Charsets.UTF_8)}"
                }
                .reduce { p1, p2 -> "$p1&$p2" }
                .orElse("")
            return HttpRequest.BodyPublishers.ofString(query)
        }
        if (mediaType == MediaType.APPLICATION_JSON) {
            val json = Jackson.toJsonString(request)
            return HttpRequest.BodyPublishers.ofString(json)
        }
        //TODO handle more media
        throw RuntimeException("No publisher support, mediaType:$mediaType")
    }

    companion object {
        private val stringBodyHandler = HttpResponse.BodyHandlers.ofString(Charsets.UTF_8)
        val cookieManager = CookieManager()
        private val httpClient: HttpClient = HttpClient.newBuilder().cookieHandler(cookieManager).build()
        private val log = LoggerFactory.getLogger(BaseRequest::class.java)
    }

    class JsonBodyHandler<T : Any>(private val type: TypeReference<T>) : HttpResponse.BodyHandler<T> {
        override fun apply(responseInfo: HttpResponse.ResponseInfo): HttpResponse.BodySubscriber<T> {
            val upstream = HttpResponse.BodySubscribers.ofInputStream()
            return HttpResponse.BodySubscribers.mapping(upstream)
            { inputStream: InputStream ->
                val string = String(inputStream.readAllBytes(), Charsets.UTF_8)
                Jackson.fromJson(string, type)
            }
        }
    }
}