package io.github.shoaky.sourcedownloader.core

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonUnwrapped
import io.github.shoaky.sourcedownloader.sdk.util.Jackson

data class PersistentPointer(
    @get:JsonUnwrapped
    @get:JsonAnyGetter
    val values: MutableMap<String, Any> = mutableMapOf()
) {

    @JsonAnySetter
    fun setValue(key: String, value: Any) {
        values[key] = value
    }

    override fun toString(): String {
        return Jackson.toJsonString(values)
    }
}