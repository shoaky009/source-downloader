package xyz.shoaky.sourcedownloader.sdk.util

import com.fasterxml.jackson.core.type.TypeReference
import java.io.InputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

object Http {
    val client: HttpClient = HttpClient.newBuilder().build()

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

fun httpGetRequest(
    uri: URI,
    headers: Map<String, String> = emptyMap()
): HttpRequest {
    val builder = HttpRequest.newBuilder(uri).GET()
    headers.forEach(builder::header)
    return builder.build()
}

fun String.find(vararg regexes: Regex): String? {
    for (regex in regexes) {
        val match = regex.find(this)
        if (match != null) {
            return match.value
        }
    }
    return null
}

fun String.replaces(replaces: List<String>, to: String): String {
    var result = this
    for (replace in replaces) {
        result = result.replace(replace, to)
    }
    return result
}