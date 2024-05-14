package io.github.shoaky.sourcedownloader.sdk

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.sdk.util.Jackson

class MapPatternVariables() : PatternVariables {
    constructor(variables: Map<String, String>) : this() {
        this.variables.putAll(variables)
    }

    constructor(variables: PatternVariables) : this() {
        this.variables.putAll(variables.variables())
    }

    constructor(any: Any) : this() {
        val map = Jackson.convert(any, jacksonTypeRef<Map<String, String>>())
        this.variables.putAll(map)
    }

    @JsonAnyGetter
    private val variables: MutableMap<String, String> = mutableMapOf()

    override fun variables(): Map<String, String> {
        return variables
    }

    fun addVariable(name: String, value: String) {
        variables[name] = value
    }

    @JsonAnySetter
    fun addVariable(name: String, value: Any) {
        // 兼容旧版本
        if (name == "variables" && value is Map<*, *>) {
            val map = value.map { it.key.toString() to it.value.toString() }.toMap()
            variables.putAll(map)
            return
        } else {
            variables[name] = value.toString()
        }
    }

    fun addVariables(variables: PatternVariables) {
        this.variables.putAll(variables.variables())
    }

    @JsonIgnore
    fun getVariables(): Map<String, String> {
        return variables
    }

    override fun toString(): String {
        return variables.toString()
    }
}