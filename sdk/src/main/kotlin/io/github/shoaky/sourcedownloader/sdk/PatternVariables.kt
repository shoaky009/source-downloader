package io.github.shoaky.sourcedownloader.sdk

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.sdk.util.Jackson

@JsonInclude(JsonInclude.Include.NON_NULL)
interface PatternVariables {

    @JsonIgnore
    fun variables(): Map<String, String> {
        return Jackson.convert(this, jacksonTypeRef())
    }

    companion object {

        val EMPTY = object : PatternVariables {}
    }
}