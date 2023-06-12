package io.github.shoaky.sourcedownloader.sdk.util

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import java.io.InputStream
import kotlin.reflect.KClass

object Jackson {

    private val objectMapper: ObjectMapper = ObjectMapper()
    private val mapRef = jacksonTypeRef<Map<String, Any>>()

    init {
        objectMapper.registerModule(KotlinModule.Builder().build())
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        objectMapper.enable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE)
        objectMapper.enable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY)
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
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

    // fun <T> fromXml(inputStream: InputStream, type: TypeReference<T>): T {
    //     return objectMapper.readValue(inputStream, type)
    // }
}