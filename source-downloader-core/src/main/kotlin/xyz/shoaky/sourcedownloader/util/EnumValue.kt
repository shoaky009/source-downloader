package xyz.shoaky.sourcedownloader.util

import java.util.*
import kotlin.reflect.KClass

interface EnumValue<T> {
    fun getValue(): T

}

fun <T, R> KClass<R>.fromValue(value: T): R where R : EnumValue<T>, R : Enum<R> {
    val enums = this.java.enumConstants
    for (enum in enums) {
        if (enum.getValue() == value) {
            return enum
        }
    }
    throw RuntimeException("Unknown value: $this")
}

fun String.find(vararg regexes: Regex): String? {
    for (regex in regexes) {
        val match = regex.find(this)
        if (match != null) {
            return match.value
        }
    }
    return null
}

fun String.encodeBase64(): String {
    return Base64.getEncoder().encodeToString(this.toByteArray())
}