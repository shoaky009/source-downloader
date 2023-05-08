package xyz.shoaky.sourcedownloader.core

import com.google.common.cache.CacheBuilder
import org.springframework.stereotype.Component
import xyz.shoaky.sourcedownloader.sdk.*
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier
import java.util.concurrent.ConcurrentHashMap

class DefaultPluginContext(
    private val componentManager: SdComponentManager,
    private val instanceManager: InstanceManager,
    private val cacheManager: MemoryCacheManager
) : PluginContext {
    override fun registerSupplier(vararg suppliers: SdComponentSupplier<*>) {
        componentManager.registerSupplier(*suppliers)
    }

    override fun registerInstanceFactory(vararg factories: InstanceFactory<*>) {
        instanceManager.registerInstanceFactory(*factories)
    }

    override fun <T> load(name: String, klass: Class<T>, props: Properties?): T {
        return instanceManager.load(name, klass, props)
    }

    override fun <T> getInstances(klass: Class<T>): List<T> {
        return instanceManager.getInstance(klass)
    }

}

@Component
class MemoryCacheManager {

    private val caches: MutableMap<String, Cache<*, *>> = ConcurrentHashMap()

    @Suppress("UNCHECKED_CAST")
    fun <K : Any, V : Any> getCache(name: String, loader: CacheLoader<K, V>): Cache<K, V> {
        val cache = caches.getOrPut(name) {
            MemoryCache(loader)
        } as? Cache<K, V> ?: throw IllegalStateException("cache $name is not a MemoryCache")
        return cache
    }
}

class MemoryCache<K : Any, V : Any>(
    private val loader: CacheLoader<K, V>
) : Cache<K, V> {

    private val cache = CacheBuilder.newBuilder().maximumSize(2000).build(
        object : com.google.common.cache.CacheLoader<K, V>() {
            override fun load(key: K): V {
                return loader.load(key)
            }
        }
    )

    override fun get(key: K): V {
        return cache.get(key)
    }
}