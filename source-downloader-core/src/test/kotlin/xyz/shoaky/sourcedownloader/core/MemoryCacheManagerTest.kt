package xyz.shoaky.sourcedownloader.core

import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.sdk.CacheLoader

class MemoryCacheManagerTest {

    private val manager = MemoryCacheManager()

    @Test
    fun test() {
        val cache1 = manager.getCache("test", object : CacheLoader<String, String> {
            override fun load(key: String): String {
                return "test1"
            }
        })

        val cache2 = manager.getCache("test", object : CacheLoader<String, String> {
            override fun load(key: String): String {
                return "test1"
            }
        })

        val cache3 = manager.getCache("test3", object : CacheLoader<String, String> {
            override fun load(key: String): String {
                return "test1"
            }
        })

        assert(cache1 === cache2)
        assert(cache1 !== cache3)
    }
}