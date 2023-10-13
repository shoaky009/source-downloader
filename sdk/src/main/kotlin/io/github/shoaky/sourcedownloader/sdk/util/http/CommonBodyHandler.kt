package io.github.shoaky.sourcedownloader.sdk.util.http

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
        "json" to Supplier<BodyMapper<T>> { CommonBodyMapper(JsonBodyMapper()) },
        "plain" to Supplier<BodyMapper<T>> { CommonBodyMapper(StringBodyMapper()) },
    )

    fun addBodyMapper(subtype: String, supplier: Supplier<BodyMapper<T>>) {
        bodyMapperSuppliers[subtype] = supplier
    }

    @Suppress("UNCHECKED_CAST")
    override fun apply(responseInfo: HttpResponse.ResponseInfo): HttpResponse.BodySubscriber<T> {
        val bodyStream = HttpResponse.BodySubscribers.ofInputStream()
        val contentType = responseInfo.headers().firstValue(HttpHeaders.CONTENT_TYPE)
        val mediaType = MediaType.parse(contentType.orElse("text/plain"))
        return HttpResponse.BodySubscribers.mapping(bodyStream)
        { inputStream: InputStream ->
            // if type is BodyWrapper
            if (type.type == BodyWrapper::class.java) {
                type as TypeReference<BodyWrapper>
                val rp = MappingInfo(type, mediaType.subtype(), inputStream.readAllBytes())
                return@mapping BodyWrapperMapper().mapping(rp) as T
            }

            val subtype = mediaType.subtype()
            for (entry in bodyMapperSuppliers) {
                if (subtype == entry.key) {
                    val rp = MappingInfo(type, subtype, inputStream.readAllBytes())
                    return@mapping entry.value.get().mapping(rp)
                }
            }

            val body = inputStream.readAllBytes().decodeToString()
            throw IllegalArgumentException("Unsupported media type: $mediaType , body:$body")
        }
    }
}