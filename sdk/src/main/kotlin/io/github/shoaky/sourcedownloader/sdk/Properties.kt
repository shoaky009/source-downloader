package io.github.shoaky.sourcedownloader.sdk

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.sdk.component.ComponentException
import io.github.shoaky.sourcedownloader.sdk.util.Jackson

class Properties private constructor(
    val rawValues: Map<String, Any>
) {

    inline fun <reified T> parse(): T {
        return Jackson.convert(rawValues, jacksonTypeRef())
    }

    inline fun <reified T> get(key: String): T {
        val any = rawValues[key] ?: throw ComponentException.props("属性${key}不存在")
        try {
            return Jackson.convert(any, jacksonTypeRef())
        } catch (e: Exception) {
            throw ComponentException.props("属性${key}解析异常: $any", e)
        }
    }

    inline fun <reified T> getOrNull(key: String): T? {
        val any = rawValues[key] ?: return null
        try {
            return Jackson.convert(any, jacksonTypeRef())
        } catch (e: Exception) {
            throw ComponentException.props("属性${key}解析异常: $any", e)
        }
    }

    fun getRaw(key: String): Any {
        return rawValues[key] ?: throw ComponentException.props("属性${key}不存在")
    }

    inline fun <reified T> getOrDefault(key: String, default: T): T {
        val any = rawValues[key] ?: return default
        try {
            return Jackson.convert(any, jacksonTypeRef())
        } catch (e: Exception) {
            throw ComponentException.props("属性解析异常: $key", e)
        }
    }

    companion object {

        @JvmStatic
        val empty = Properties(mutableMapOf())

        @JvmStatic
        fun fromMap(map: Map<String, Any>): Properties {
            val mutableMapOf = mutableMapOf<String, Any>()
            mutableMapOf.putAll(map)
            for (entry in map) {
                val value = entry.value
                if (value is Map<*, *> && value["0"] != null) {
                    // yaml中的数组会被解析成map
                    mutableMapOf[entry.key] = value.values
                }
            }

            return Properties(mutableMapOf)
        }

        @JvmStatic
        fun fromJson(json: String): Properties {
            val typeReference = jacksonTypeRef<MutableMap<String, Any>>()
            return Properties(Jackson.fromJson(json, typeReference))
        }
    }
}

