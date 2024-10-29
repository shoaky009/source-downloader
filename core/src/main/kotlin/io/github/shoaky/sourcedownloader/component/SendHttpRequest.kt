package io.github.shoaky.sourcedownloader.component

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import com.google.common.net.HttpHeaders
import com.google.common.net.MediaType
import com.google.common.net.UrlEscapers
import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.ProcessContext
import io.github.shoaky.sourcedownloader.sdk.component.ProcessListener
import io.github.shoaky.sourcedownloader.sdk.http.HttpMethod
import io.github.shoaky.sourcedownloader.sdk.util.Jackson
import io.github.shoaky.sourcedownloader.sdk.util.http.httpClient
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers
import java.time.Duration

/**
 * 发送一个HTTP请求
 */
class SendHttpRequest(
    private val config: HttpRequestConfig
) : ProcessListener {

    override fun onItemSuccess(context: ProcessContext, itemContent: ItemContent) {
        val uri = buildUri(mapOf("summary" to itemContent.summaryContent()))
        val headers = mutableMapOf<String, String>()
        headers.putAll(config.headers)
        val bodyPublishers = buildBodyPublisher(context, itemContent, headers)

        val request = HttpRequest.newBuilder(uri)
            .method(config.method.name, bodyPublishers)
            .timeout(Duration.ofSeconds(30))

        headers.forEach(request::setHeader)
        val response = try {
            httpClient.send(request.build(), BodyHandlers.discarding())
        } catch (e: Exception) {
            log.error("Send http request to $uri failed, detail:{}", e.message)
            throw e
        }

        if (isNot2XX(response.statusCode())) {
            log.warn("Send http request to $uri, response code is ${response.statusCode()}")
        }
    }

    private fun buildUri(vars: Map<String, String>): URI {
        val split = config.url.split("?", limit = 2)
        val base = split[0]
        val keyValues = split.getOrNull(1)?.split("&") ?: emptyList()
        var url = base
        keyValues.forEachIndexed { index, keyValue ->
            val pair = keyValue.split("=")
            val name = pair.getOrNull(0) ?: return@forEachIndexed
            var value = pair.getOrNull(1) ?: ""
            vars.forEach { (key, v) ->
                value = value.replace("{$key}", v)
            }
            val encodedValue = UrlEscapers.urlFragmentEscaper().escape(value)
            url += if (index == 0) "?$name=$encodedValue" else "&$name=$encodedValue"
        }
        return URI(url)
    }

    override fun onProcessCompleted(processContext: ProcessContext) {
        val processedItems = processContext.processedItems()
        val size = processedItems.size

        val headers = mutableMapOf<String, String>()
        headers.putAll(config.headers)
        val bodyPublishers = buildBodyPublisher(processContext, headers)

        val uri = buildUri(mapOf("summary" to "Processed $size items"))
        val request = HttpRequest.newBuilder(uri).method(config.method.name, bodyPublishers)

        headers.forEach(request::setHeader)
        val response = httpClient.send(request.build(), BodyHandlers.discarding())
        if (isNot2XX(response.statusCode())) {
            log.warn("Send http request to $uri, response code is ${response.statusCode()}")
        }
    }

    private fun isNot2XX(statusCode: Int): Boolean {
        return statusCode.toString().startsWith("2").not()
    }

    private fun buildBodyPublisher(
        context: ProcessContext, content: ItemContent, headers: MutableMap<String, String>
    ): HttpRequest.BodyPublisher {
        return if (config.body.isNullOrBlank().not()) {
            val body = config.body?.replace("{summary}", content.summaryContent())
            BodyPublishers.ofString(body)
        } else if (config.withContentBody) {
            headers[HttpHeaders.CONTENT_TYPE] = MediaType.JSON_UTF_8.toString()
            BodyPublishers.ofString(
                Jackson.toJsonString(
                    mapOf("content" to content, "processor" to context.processor())
                )
            )
        } else {
            BodyPublishers.noBody()
        }
    }

    private fun buildBodyPublisher(
        context: ProcessContext, headers: MutableMap<String, String>
    ): HttpRequest.BodyPublisher {
        val contents = context.processedItems().map {
            context.getItemContent(it)
        }
        return if (config.body.isNullOrBlank().not()) {
            val body = config.body?.replace("{summary}", "Processed ${contents.size} items")
            BodyPublishers.ofString(body)
        } else if (config.withContentBody) {
            headers[HttpHeaders.CONTENT_TYPE] = MediaType.JSON_UTF_8.toString()
            BodyPublishers.ofString(
                Jackson.toJsonString(
                    mapOf("contents" to contents, "processor" to context.processor())
                )
            )
        } else {
            BodyPublishers.noBody()
        }
    }

    companion object {

        private val log = LoggerFactory.getLogger(SendHttpRequest::class.java)
    }

    data class HttpRequestConfig(
        val url: String,
        @JsonSerialize(using = ToStringSerializer::class)
        val method: HttpMethod = HttpMethod.POST,
        val headers: Map<String, String> = emptyMap(),
        val body: String? = null,
        @JsonAlias("with-content-body") val withContentBody: Boolean = false
    )

}

