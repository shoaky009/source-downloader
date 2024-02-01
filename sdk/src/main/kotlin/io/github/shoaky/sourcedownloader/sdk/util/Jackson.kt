package io.github.shoaky.sourcedownloader.sdk.util

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import java.io.InputStream
import java.nio.file.Path
import kotlin.reflect.KClass

// TODO 支持kebab-case和camelCase
object Jackson {

    private val objectMapper: ObjectMapper = ObjectMapper()
    private val mapRef = jacksonTypeRef<Map<String, Any>>()

    init {
        objectMapper
            .registerModule(KotlinModule.Builder().build())
            .registerModule(JavaTimeModule())
            .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE)
            .enable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .addHandler(DefaultHandler)
    }

    fun <T : Any> fromJson(json: String, type: KClass<T>): T {
        return objectMapper.readValue(json, type.java)
    }

    fun <T : Any> fromJson(inputStream: InputStream, type: KClass<T>): T {
        return objectMapper.readValue(inputStream, type.java)
    }

    fun <T : Any> fromJson(json: String, type: TypeReference<T>): T {
        return objectMapper.readValue(json, type)
    }

    fun <T : Any> fromJson(bytes: ByteArray, type: TypeReference<T>): T {
        return objectMapper.readValue(bytes, type)
    }

    fun <T : Any> fromJson(inputStream: InputStream, type: TypeReference<T>): T {
        return objectMapper.readValue(inputStream, type)
    }

    fun <T : Any> convert(data: Any, type: KClass<T>): T {
        return objectMapper.convertValue(data, type.java)
    }

    fun <T> convert(data: Any, type: TypeReference<T>): T {
        return objectMapper.convertValue(data, type)
    }

    fun toByteArray(data: Any): ByteArray {
        return objectMapper.writeValueAsBytes(data)
    }

    fun toJsonString(data: Any): String {
        return objectMapper.writeValueAsString(data)
    }

    fun convertToMap(any: Any): Map<String, Any> {
        return convert(any, mapRef)
    }

}

private object DefaultHandler : DeserializationProblemHandler() {

    override fun handleInstantiationProblem(
        ctxt: DeserializationContext, instClass: Class<*>, argument: Any, t: Throwable
    ): Any {
        if (instClass == Path::class.java) {
            return Path.of(argument.toString())
        }
        return super.handleInstantiationProblem(ctxt, instClass, argument, t)
    }

}

class JoinStringSerializer : StdSerializer<Collection<*>>(Collection::class.java) {

    override fun serialize(value: Collection<*>, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString(value.joinToString(","))
    }

}