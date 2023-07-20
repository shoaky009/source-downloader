package io.github.shoaky.sourcedownloader.component

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import io.github.shoaky.sourcedownloader.SourceDownloaderApplication.Companion.log
import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.component.ComponentStateful
import io.github.shoaky.sourcedownloader.sdk.component.RunAfterCompletion
import io.github.shoaky.sourcedownloader.sdk.util.Jackson
import io.github.shoaky.sourcedownloader.sdk.util.http.httpClient
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.util.UriComponentsBuilder
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers

class SendHttpRequest(
    private val props: Props
) : RunAfterCompletion, ComponentStateful {

    override fun accept(t: ItemContent) {
        val uriComponents = UriComponentsBuilder.fromHttpUrl(props.url)
            .encode().build().expand(
                mapOf("summary" to t.summaryContent())
            )

        val headers = mutableMapOf<String, String>()
        headers.putAll(props.headers)
        val bodyPublishers = buildBodyPublisher(t, headers)

        val uri = uriComponents.toUri()
        val request = HttpRequest.newBuilder(uri)
            .method(props.method.name(), bodyPublishers)

        headers.forEach(request::setHeader)
        val response = httpClient.send(request.build(), BodyHandlers.discarding())
        if (response.statusCode() != HttpStatus.OK.value()) {
            log.warn("send http request to $uri, response code is ${response.statusCode()}")
        }
    }

    private fun buildBodyPublisher(
        content: ItemContent,
        headers: MutableMap<String, String>
    ): HttpRequest.BodyPublisher {
        return if (props.body.isNullOrBlank().not()) {
            val body = props.body?.replace("{summary}", content.summaryContent())
            BodyPublishers.ofString(body)
        } else if (props.withContentBody) {
            headers[HttpHeaders.CONTENT_TYPE] = MediaType.APPLICATION_JSON_VALUE
            BodyPublishers.ofString(Jackson.toJsonString(content))
        } else {
            BodyPublishers.noBody()
        }
    }

    data class Props(
        val url: String,
        @JsonSerialize(using = ToStringSerializer::class)
        val method: HttpMethod = HttpMethod.POST,
        val headers: Map<String, String> = emptyMap(),
        val body: String? = null,
        @JsonAlias("with-content-body")
        val withContentBody: Boolean = false
    )

    override fun stateDetail(): Any {
        return props
    }
}

