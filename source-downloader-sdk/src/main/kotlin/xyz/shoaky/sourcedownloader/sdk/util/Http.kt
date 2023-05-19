package xyz.shoaky.sourcedownloader.sdk.util

import com.fasterxml.jackson.core.type.TypeReference
import com.google.common.net.HttpHeaders
import com.google.common.net.MediaType
import java.io.InputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*
import java.util.function.Supplier

object Http {
    val client: HttpClient = HttpClient.newBuilder().build()

    class CommonBodyHandler<T : Any>(
        private val type: TypeReference<T>
    ) : HttpResponse.BodyHandler<T> {

        private val bodyMapperSuppliers: MutableMap<String, Supplier<BodyMapper<T>>> = mutableMapOf(
            "json" to Supplier<BodyMapper<T>> {
                object : BodyMapper<T> {
                    override fun mapping(info: MappingInfo<T>): T {
                        return Jackson.fromJson(info.inputStream, type)
                    }
                }
            },
            // "xml" to Supplier<BodyMapper<T>> {
            //     object : BodyMapper<T> {
            //         override fun mapping(info: MappingInfo<T>): T {
            //             return Jackson.fromXml(info.inputStream, type)
            //         }
            //     }
            // },
            "plain" to Supplier<BodyMapper<T>> {
                object : BodyMapper<T> {
                    override fun mapping(info: MappingInfo<T>): T {
                        val string = String(info.inputStream.readAllBytes(), Charsets.UTF_8)
                        return Jackson.convert(string, type)
                    }
                }
            },
        )

        fun addBodyMapper(subtype: String, supplier: Supplier<BodyMapper<T>>) {
            bodyMapperSuppliers[subtype] = supplier
        }

        override fun apply(responseInfo: HttpResponse.ResponseInfo): HttpResponse.BodySubscriber<T> {
            val bodyStream = HttpResponse.BodySubscribers.ofInputStream()
            val contentType = responseInfo.headers().firstValue(HttpHeaders.CONTENT_TYPE)
            val mediaType = MediaType.parse(contentType.orElse("text/plain"))
            return HttpResponse.BodySubscribers.mapping(bodyStream)
            { inputStream: InputStream ->
                val subtype = mediaType.subtype()
                for (entry in bodyMapperSuppliers) {
                    val rp = MappingInfo(type, subtype, inputStream)
                    if (subtype == entry.key) {
                        return@mapping entry.value.get().mapping(rp)
                    }
                }

                val body = inputStream.readAllBytes().decodeToString()
                throw IllegalArgumentException("Unsupported media type: $mediaType , body:$body")
            }
        }
    }

    interface BodyMapper<T : Any> {

        fun mapping(info: MappingInfo<T>): T
    }

    data class MappingInfo<T : Any>(
        val type: TypeReference<T>,
        val subtype: String,
        val inputStream: InputStream
    )
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

fun String.encodeBase64(): String {
    return Base64.getEncoder().encodeToString(this.toByteArray())
}