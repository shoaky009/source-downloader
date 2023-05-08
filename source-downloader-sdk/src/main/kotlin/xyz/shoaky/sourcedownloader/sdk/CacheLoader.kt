package xyz.shoaky.sourcedownloader.sdk

interface CacheLoader<K, V> {

    fun load(key: K): V
}