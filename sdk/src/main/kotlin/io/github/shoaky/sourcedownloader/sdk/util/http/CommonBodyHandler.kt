package io.github.shoaky.sourcedownloader.sdk.util.http

import com.fasterxml.jackson.core.type.TypeReference
import com.google.common.net.HttpHeaders
import com.google.common.net.MediaType
import java.io.InputStream
import java.net.http.HttpResponse
import java.util.function.Supplier

class CommonBodyHandler<T : Any>(
    private val type: TypeReference<T>,
    /**
     * Default value for body, if body is null or response unexpected content will return default value
     */
    private val default: T? = null
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
                val rp = MappingInfo(responseInfo, type, mediaType.subtype(), inputStream.readAllBytes())
                return@mapping BodyWrapperMapper().mapping(rp) as T
            }

            val subtype = mediaType.subtype()
            for (entry in bodyMapperSuppliers) {
                if (subtype == entry.key) {
                    val rp = MappingInfo(responseInfo, type, subtype, inputStream.readAllBytes())

                    try {
                        return@mapping entry.value.get().mapping(rp)
                    } catch (e: Exception) {
                        val body = String(rp.bytes, Charsets.UTF_8)
                        throw BodyMappingException(
                            body,
                            responseInfo.statusCode(),
                            responseInfo.headers(),
                            "Failed to mapping body:$body, statusCode:${responseInfo.statusCode()}, message:${e.message}"
                        )
                    }
                }
            }

            val body = inputStream.readAllBytes().decodeToString()
            if (default != null) {
                return@mapping default
            }
            throw IllegalArgumentException("Unsupported media type: $mediaType , body:$body")
        }
    }
}