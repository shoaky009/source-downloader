package io.github.shoaky.sourcedownloader.component

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import io.github.shoaky.sourcedownloader.SourceDownloaderApplication.Companion.log
import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.ProcessContext
import io.github.shoaky.sourcedownloader.sdk.component.ProcessListener
import io.github.shoaky.sourcedownloader.sdk.util.Jackson
import io.github.shoaky.sourcedownloader.sdk.util.http.httpClient
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.web.util.UriComponentsBuilder
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers

/**
 * 发送一个HTTP请求
 */
class SendHttpRequest(
    private val config: HttpRequestConfig
) : ProcessListener {

    override fun onItemSuccess(context: ProcessContext, itemContent: ItemContent) {
        val uriComponents = UriComponentsBuilder.fromHttpUrl(config.url).encode().build().expand(
                mapOf("summary" to itemContent.summaryContent())
            )

        val headers = mutableMapOf<String, String>()
        headers.putAll(config.headers)
        val bodyPublishers = buildBodyPublisher(context, itemContent, headers)

        val uri = uriComponents.toUri()
        val request = HttpRequest.newBuilder(uri).method(config.method.name(), bodyPublishers)

        headers.forEach(request::setHeader)
        val response = httpClient.send(request.build(), BodyHandlers.discarding())
        if (isNot2XX(response.statusCode())) {
            log.warn("Send http request to $uri, response code is ${response.statusCode()}")
        }
    }

    override fun onProcessCompleted(processContext: ProcessContext) {
        val processedItems = processContext.processedItems()
        val size = processedItems.size
        val uriComponents = UriComponentsBuilder.fromHttpUrl(config.url).encode().build().expand(
                mapOf("summary" to "Processed $size items")
            )

        val headers = mutableMapOf<String, String>()
        headers.putAll(config.headers)
        val bodyPublishers = buildBodyPublisher(processContext, headers)

        val uri = uriComponents.toUri()
        val request = HttpRequest.newBuilder(uri).method(config.method.name(), bodyPublishers)

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
            headers[HttpHeaders.CONTENT_TYPE] = MediaType.APPLICATION_JSON_VALUE
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
            headers[HttpHeaders.CONTENT_TYPE] = MediaType.APPLICATION_JSON_VALUE
            BodyPublishers.ofString(
                Jackson.toJsonString(
                    mapOf("contents" to contents, "processor" to context.processor())
                )
            )
        } else {
            BodyPublishers.noBody()
        }
    }

    data class HttpRequestConfig(
        val url: String,
        @JsonSerialize(using = ToStringSerializer::class) val method: HttpMethod = HttpMethod.POST,
        val headers: Map<String, String> = emptyMap(),
        val body: String? = null,
        @JsonAlias("with-content-body") val withContentBody: Boolean = false
    )

}

