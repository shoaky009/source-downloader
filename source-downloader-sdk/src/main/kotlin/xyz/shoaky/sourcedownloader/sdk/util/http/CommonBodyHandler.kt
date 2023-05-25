package xyz.shoaky.sourcedownloader.sdk.util.http

import com.fasterxml.jackson.core.type.TypeReference
import com.google.common.net.HttpHeaders
import com.google.common.net.MediaType
import java.io.InputStream
import java.net.http.HttpResponse
import java.util.function.Supplier

class CommonBodyHandler<T : Any>(
    private val type: TypeReference<T>
) : HttpResponse.BodyHandler<T> {

    private val bodyMapperSuppliers: MutableMap<String, Supplier<BodyMapper<T>>> = mutableMapOf(
        "json" to Supplier<BodyMapper<T>> { WarpBodyMapper(JsonBodyMapper()) },
        "plain" to Supplier<BodyMapper<T>> { WarpBodyMapper(StringBodyMapper()) },
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