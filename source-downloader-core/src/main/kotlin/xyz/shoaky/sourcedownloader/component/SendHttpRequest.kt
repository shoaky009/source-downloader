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
        val url = props.url
        var uriComponents = UriComponentsBuilder.fromHttpUrl(url)
            .encode().build()
        uriComponents = uriComponents.expand(mapOf("summary" to t.summarySubject()))

        val client = Http.client

        val body = BodyPublishers.ofString(Jackson.toJsonString(t))
        val request = HttpRequest.newBuilder(uriComponents.toUri())
            .method(props.method.name(), body)

        props.headers.forEach(request::setHeader)
        val response = client.send(request.build(), BodyHandlers.discarding())
        if (response.statusCode() != 200) {
            log.warn("send http request to $url, response code is ${response.statusCode()}")
        }
    }

    data class Props(
        val url: String,
        val method: HttpMethod = HttpMethod.POST,
        val headers: Map<String, String> = emptyMap(),
        val withSummary: Boolean = true,
        val withContent: Boolean = true
    )
}

