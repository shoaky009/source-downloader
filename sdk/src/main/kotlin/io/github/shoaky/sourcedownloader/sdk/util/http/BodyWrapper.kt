package io.github.shoaky.sourcedownloader.sdk.util.http

import com.fasterxml.jackson.core.type.TypeReference
import io.github.shoaky.sourcedownloader.sdk.util.Jackson

class BodyWrapper(
    private val raw: ByteArray
) {

    fun <T : Any> parseJson(typeRef: TypeReference<T>): T {
        return Jackson.fromJson(raw, typeRef)
    }

    fun stringify(): String {
        return String(raw, Charsets.UTF_8)
    }
}