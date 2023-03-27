package xyz.shoaky.sourcedownloader.sdk

import com.fasterxml.jackson.core.type.TypeReference
import xyz.shoaky.sourcedownloader.sdk.util.Jackson

interface PatternVariables {

    fun getVariables(): Map<String, String> {
        return Jackson.convert(this, object : TypeReference<Map<String, String>>() {})
    }
}