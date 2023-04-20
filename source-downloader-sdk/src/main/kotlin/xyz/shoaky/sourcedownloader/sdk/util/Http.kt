package xyz.shoaky.sourcedownloader.sdk.util

import com.fasterxml.jackson.core.type.TypeReference
import java.io.InputStream
import java.net.http.HttpClient
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
fun String.find(vararg regexes: Regex): String? {
    for (regex in regexes) {
        val match = regex.find(this)
        if (match != null) {
            return match.value
        }
    }
    return null
}