package xyz.shoaky.sourcedownloader.core.component

import org.springframework.http.HttpMethod
import org.springframework.web.util.UriComponentsBuilder
import xyz.shoaky.sourcedownloader.SourceDownloaderApplication.Companion.log
import xyz.shoaky.sourcedownloader.sdk.SourceContent
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.RunAfterCompletion
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier
import xyz.shoaky.sourcedownloader.sdk.util.Http
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
        val request = HttpRequest.newBuilder(uriComponents.toUri())
            .method(props.method.name(), BodyPublishers.noBody())

        props.headers.forEach(request::setHeader)
        val response = client.send(request.build(), BodyHandlers.discarding())
        if (response.statusCode() != 200) {
            log.warn("send http request to $url, response code is ${response.statusCode()}")
        }
    }

    data class Props(
        val url: String,
        val method: HttpMethod = HttpMethod.GET,
        val headers: Map<String, String> = emptyMap(),
        val withSummary: Boolean = true
    )
}

object SendHttpRequestSupplier : SdComponentSupplier<SendHttpRequest> {
    override fun apply(props: ComponentProps): SendHttpRequest {
        return SendHttpRequest(props.parse())
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.run("http")
        )
    }

    override fun getComponentClass(): Class<SendHttpRequest> {
        return SendHttpRequest::class.java
    }

}