package io.github.shoaky.sourcedownloader.core

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonUnwrapped

data class PersistentPointer(
    @get:JsonUnwrapped
    @get:JsonAnyGetter
    val values: MutableMap<String, Any>
) {

    @JsonAnySetter
    fun setValue(key: String, value: Any) {
        values[key] = value
    }

}