package io.github.shoaky.sourcedownloader.sdk

data class SimpleItemPointer<T>(
    val id: String,
    val value: T
) : ItemPointer