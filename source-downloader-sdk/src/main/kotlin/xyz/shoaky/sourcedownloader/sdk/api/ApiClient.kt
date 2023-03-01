package xyz.shoaky.sourcedownloader.sdk.api

import com.fasterxml.jackson.core.type.TypeReference
import com.google.common.net.HttpHeaders
import com.google.common.net.MediaType
import org.slf4j.LoggerFactory
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
            .method(request.httpMethod.name, bodyPublisher(request))
        request.setHeader(HttpHeaders.CONTENT_TYPE, request.mediaType)
        request.httpHeaders().forEach { (name, value) -> requestBuilder.header(name, value) }

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
        if (request.mediaType == MediaType.JSON_UTF_8) {
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
        if (mediaType == MediaType.FORM_DATA || mediaType == MediaType.PLAIN_TEXT_UTF_8) {
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
        if (mediaType == MediaType.JSON_UTF_8) {
            val json = Jackson.toJsonString(request)
            return HttpRequest.BodyPublishers.ofString(json)
        }
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