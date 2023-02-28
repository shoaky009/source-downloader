package xyz.shoaky.sourcedownloader.sdk.api

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.core.type.TypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType

abstract class BaseRequest<T : Any> {

    @get:JsonIgnore
    abstract val path: String

    @get:JsonIgnore
    abstract val responseBodyType: TypeReference<T>

    @get:JsonIgnore
    abstract val httpMethod: HttpMethod

    @get:JsonIgnore
    abstract val mediaType: MediaType

    @JsonIgnore
    val queryString = mutableMapOf<String, Any>()

    @JsonIgnore
    val httpHeaders = HttpHeaders()

    protected fun addQueryParameter(name: String, value: Any) {
        queryString[name] = value
    }

    companion object {
        val stringTypeReference = object : TypeReference<String>() {}
    }

}