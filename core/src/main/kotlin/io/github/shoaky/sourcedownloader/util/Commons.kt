package io.github.shoaky.sourcedownloader.util

import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.io.path.notExists
import kotlin.io.path.readAttributes

fun Path.creationTime(): LocalDateTime? {
    if (this.notExists()) {
        return null
    }
    val attrs = this.readAttributes<BasicFileAttributes>()
    val creationTime = attrs.creationTime()
    return LocalDateTime.ofInstant(creationTime.toInstant(), ZoneId.systemDefault())
}

fun <T, K> MutableMap<K, List<T>>.addToCollection(key: K, value: T) {
    if (this.containsKey(key)) {
        this[key] = this.getValue(key) + value
    } else {
        this[key] = listOf(value)
    }
}