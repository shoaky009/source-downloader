package io.github.shoaky.sourcedownloader.sdk.api

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.google.common.net.MediaType
import com.google.common.net.UrlEscapers
import io.github.shoaky.sourcedownloader.sdk.util.Jackson
import io.github.shoaky.sourcedownloader.sdk.util.appendPrefix
import io.github.shoaky.sourcedownloader.sdk.util.http.httpClient
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse

typealias GHttpHeaders = com.google.common.net.HttpHeaders

interface ApiClient {

    fun <R : BaseRequest<T>, T : Any> execute(endpoint: URI, request: R): HttpResponse<T>

}

abstract class HookedApiClient(
    val client: HttpClient = httpClient,
) : ApiClient {

    override fun <R : BaseRequest<T>, T : Any> execute(endpoint: URI, request: R): HttpResponse<T> {
        val requestBuilder = HttpRequest.newBuilder(endpoint)
        beforeRequest(requestBuilder, request)
        val path = request.path.appendPrefix('/')

        val uriString = StringBuilder(endpoint.toString().removeSuffix("/"))
            .append(UrlEscapers.urlFragmentEscaper().escape(path))
            .toString()
        val uri = URI(uriString)
        val queryString = buildQueryString(request, uri)
        val resolve = uriString + queryString
        requestBuilder.uri(URI(resolve))

        val bodyPublisher = if (request.httpMethod == HttpMethod.GET.name) {
            BodyPublishers.noBody()
        } else {
            bodyPublisher(request)
        }
        requestBuilder.method(request.httpMethod, bodyPublisher)
        request.mediaType?.let {
            request.setHeader(GHttpHeaders.CONTENT_TYPE, it)
        }
        request.httpHeaders().forEach { (name, value) -> requestBuilder.header(name, value) }

        val httpRequest = requestBuilder.build()

        val httpResponse = try {
            client.send(httpRequest, request.bodyHandler())
        } catch (e: Exception) {
            throw e
        }
        if (log.isDebugEnabled) {
            log.debug(
                """
                request:{}
                requestBody:{}
                responseCode:{}
                responseBody:{}
                """, httpRequest.uri(), Jackson.toJsonString(request),
                httpResponse.statusCode(), httpResponse.body())
        }
        afterRequest(httpResponse, request)
        return httpResponse as HttpResponse<T>
    }

    private fun <R : BaseRequest<T>, T : Any> buildQueryString(request: R, uri: URI): String {
        val queryStringMap = mutableMapOf<String, String>()
        if (request.httpMethod === HttpMethod.GET.name) {
            val convertToMap = Jackson.convert(request, jacksonTypeRef<Map<String, String?>>())
            queryStringMap.putAll(convertToMap.filterNotNullValues())

        }

        request.queryString.forEach { (k, v) ->
            queryStringMap[k] = v.toString()
        }
        if (queryStringMap.isEmpty()) {
            return ""
        }

        var queryString = queryStringMap.map { "${it.key}=${URLEncoder.encode(it.value, Charsets.UTF_8)}" }
            .joinToString("&")

        queryString = if (uri.query?.isEmpty() != false) {
            "?$queryString"
        } else {
            "&$queryString"
        }
        return queryString
    }

    abstract fun <R : BaseRequest<T>, T : Any> beforeRequest(requestBuilder: HttpRequest.Builder, request: R)

    abstract fun <R : BaseRequest<T>, T : Any> afterRequest(response: HttpResponse<T>, request: R)

    private fun <R : BaseRequest<T>, T : Any> bodyPublisher(request: BaseRequest<T>): HttpRequest.BodyPublisher {
        request.bodyPublisher()?.let {
            return it
        }

        val mediaType = request.mediaType
        if (mediaType == MediaType.FORM_DATA || mediaType == MediaType.PLAIN_TEXT_UTF_8) {
            val data = Jackson.convert(request, jacksonTypeRef<Map<String, Any?>>())
            val query = data.entries.stream()
                .filter { (_, v) -> v != null }
                .map { (k, v) ->
                    "${URLEncoder.encode(k, Charsets.UTF_8)}=${URLEncoder.encode(v.toString(), Charsets.UTF_8)}"
                }
                .reduce { p1, p2 -> "$p1&$p2" }
                .orElse("")
            return BodyPublishers.ofString(query)
        }
        if (mediaType == MediaType.JSON_UTF_8) {
            val json = Jackson.toJsonString(request)
            return BodyPublishers.ofString(json)
        }
        throw RuntimeException("No publisher support, mediaType:$mediaType")
    }

    companion object {

        private val log = LoggerFactory.getLogger(BaseRequest::class.java)

        private fun <K, V> Map<K, V?>.filterNotNullValues(): Map<K, V> =
            mutableMapOf<K, V>().apply {
                for ((k, v) in this@filterNotNullValues) if (v != null) put(k, v)
            }
    }

}