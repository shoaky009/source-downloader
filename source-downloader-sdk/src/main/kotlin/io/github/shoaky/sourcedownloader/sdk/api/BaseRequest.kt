package io.github.shoaky.sourcedownloader.sdk.api

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.google.common.net.MediaType
import io.github.shoaky.sourcedownloader.sdk.util.http.CommonBodyHandler

abstract class BaseRequest<T : Any> {

    @get:JsonIgnore
    abstract val path: String

    @get:JsonIgnore
    protected abstract val responseBodyType: TypeReference<T>

    @get:JsonIgnore
    abstract val httpMethod: String

    @get:JsonIgnore
    abstract val mediaType: MediaType?

    @JsonIgnore
    val queryString = mutableMapOf<String, Any>()

    @JsonIgnore
    protected val httpHeaders = mutableMapOf<String, String>()

    open fun bodyHandler(): CommonBodyHandler<T> {
        return CommonBodyHandler(this.responseBodyType)
    }

    protected fun addQueryParameter(name: String, value: Any) {
        queryString[name] = value
    }

    fun httpHeaders(): Map<String, String> {
        return httpHeaders
    }

    fun setHeader(name: String, value: Any) {
        httpHeaders[name] = value.toString()
    }

    companion object {
        val stringTypeReference = jacksonTypeRef<String>()
    }

}

enum class HttpMethod {
    GET, POST, PUT, DELETE
}