package io.github.shoaky.sourcedownloader.core

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonUnwrapped
import io.github.shoaky.sourcedownloader.sdk.SourceItemPointer
import java.io.Serializable

data class PersistentItemPointer(
    @get:JsonUnwrapped
    @get:JsonAnyGetter
    val values: MutableMap<String, Any>
) : SourceItemPointer, Serializable {

    @JsonAnySetter
    fun setValue(key: String, value: Any) {
        values[key] = value
    }
}