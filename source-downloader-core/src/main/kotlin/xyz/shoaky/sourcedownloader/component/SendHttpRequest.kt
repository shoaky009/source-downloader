package xyz.shoaky.sourcedownloader.component

import org.springframework.http.HttpMethod
import org.springframework.web.util.UriComponentsBuilder
import xyz.shoaky.sourcedownloader.SourceDownloaderApplication.Companion.log
import xyz.shoaky.sourcedownloader.sdk.SourceContent
import xyz.shoaky.sourcedownloader.sdk.component.RunAfterCompletion
import xyz.shoaky.sourcedownloader.sdk.util.Http
import xyz.shoaky.sourcedownloader.sdk.util.Jackson
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers

class SendHttpRequest(
    private val props: Props
) : RunAfterCompletion {
    override fun accept(t: SourceContent) {
        val uriComponents = UriComponentsBuilder.fromHttpUrl(props.url)
            .encode().build().expand(
                mapOf("summary" to t.summaryContent())
            )

        val bodyPublishers = buildBodyPublisher(t)

        val uri = uriComponents.toUri()
        val request = HttpRequest.newBuilder(uri)
            .method(props.method.name(), bodyPublishers)

        props.headers.forEach(request::setHeader)
        val response = Http.client.send(request.build(), BodyHandlers.discarding())
        if (response.statusCode() != 200) {
            log.warn("send http request to $uri, response code is ${response.statusCode()}")
        }
    }

    private fun buildBodyPublisher(content: SourceContent): HttpRequest.BodyPublisher? {
        val bodyPublishers = if (props.withContent) {
            BodyPublishers.ofString(Jackson.toJsonString(content))
        } else if (props.body.isNullOrBlank().not()) {
            val body = props.body?.replace("{summary}", content.summaryContent())
            BodyPublishers.ofString(body)
        } else {
            BodyPublishers.noBody()
        }
        return bodyPublishers
    }

    data class Props(
        val url: String,
        val method: HttpMethod = HttpMethod.POST,
        val headers: Map<String, String> = emptyMap(),
        val body: String? = null,
        val withContent: Boolean = false
    )
}

