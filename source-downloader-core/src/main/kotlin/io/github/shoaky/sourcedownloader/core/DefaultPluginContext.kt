package io.github.shoaky.sourcedownloader.core

import com.google.common.cache.CacheBuilder
import io.github.shoaky.sourcedownloader.config.SourceDownloaderProperties
import io.github.shoaky.sourcedownloader.core.component.ComponentManager
import io.github.shoaky.sourcedownloader.sdk.*
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import org.springframework.stereotype.Component
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

class DefaultPluginContext(
    private val componentManager: ComponentManager,
    private val instanceManager: InstanceManager,
    private val cacheManager: MemoryCacheManager,
    // 这个不应该这样注入, 不过暂时没有其他应用级别的需求
    private val applicationProps: SourceDownloaderProperties
) : PluginContext {

    override fun getPersistentDataPath(): Path {
        return applicationProps.dataLocation
    }

    override fun registerSupplier(vararg suppliers: ComponentSupplier<*>) {
        componentManager.registerSupplier(*suppliers)
    }

    override fun registerInstanceFactory(vararg factories: InstanceFactory<*>) {
        instanceManager.registerInstanceFactory(*factories)
    }

    override fun <T> load(name: String, klass: Class<T>, props: Properties?): T {
        return instanceManager.load(name, klass, props)
    }

    override fun getInstanceManager(): InstanceManager {
        return instanceManager
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