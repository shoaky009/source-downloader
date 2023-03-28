package xyz.shoaky.sourcedownloader.sdk

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.core.type.TypeReference
import xyz.shoaky.sourcedownloader.sdk.util.Jackson

interface PatternVariables {

    @JsonIgnore
    fun variables(): Map<String, String> {
        return Jackson.convert(this, object : TypeReference<Map<String, String>>() {})
    }
}