package io.github.shoaky.sourcedownloader.core

import io.github.shoaky.sourcedownloader.config.SourceDownloaderProperties
import io.github.shoaky.sourcedownloader.core.component.ComponentManager
import io.github.shoaky.sourcedownloader.core.processor.log
import io.github.shoaky.sourcedownloader.sdk.InstanceManager
import io.github.shoaky.sourcedownloader.sdk.plugin.Plugin
import org.springframework.stereotype.Component
import java.util.*

@Component
class PluginManager(
    componentManager: ComponentManager,
    instanceManager: InstanceManager,
    cacheManager: MemoryCacheManager,
    applicationProps: SourceDownloaderProperties
) {

    private val pluginContext = DefaultPluginContext(
        componentManager,
        instanceManager,
        cacheManager,
        applicationProps
    )
    private val plugins = Collections.synchronizedList(mutableListOf<Plugin>())
    private val pluginLoader = ServiceLoaderPluginLoader
    fun loadPlugins() {
        val loadedPlugins = pluginLoader.loadPlugins()
        log.info("Loaded plugins: ${loadedPlugins.map { it.description().fullName() }}")
        plugins.addAll(loadedPlugins)
    }

    fun initPlugins() {
        plugins.forEach { it.init(pluginContext) }
    }

    fun getPlugins(): List<Plugin> {
        return plugins
    }

    fun destroyPlugins() {
        plugins.forEach { it.destroy(pluginContext) }
    }

    private object ServiceLoaderPluginLoader : PluginLoader {

        override fun loadPlugins(classLoader: ClassLoader?): List<Plugin> {
            return ServiceLoader.load(Plugin::class.java, Thread.currentThread().contextClassLoader).map { it }
        }
    }
}

interface PluginLoader {

    fun loadPlugins(classLoader: ClassLoader? = null): List<Plugin>
}
