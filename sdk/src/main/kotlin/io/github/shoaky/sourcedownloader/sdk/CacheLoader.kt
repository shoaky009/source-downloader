package io.github.shoaky.sourcedownloader.sdk

interface CacheLoader<K, V> {

    fun load(key: K): V
}