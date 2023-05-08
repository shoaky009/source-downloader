package xyz.shoaky.sourcedownloader.sdk

interface Cache<K, V> {

    fun get(key: K): V
}