package io.github.shoaky.sourcedownloader.util

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