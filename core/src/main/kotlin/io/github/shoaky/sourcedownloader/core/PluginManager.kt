package io.github.shoaky.sourcedownloader.core

import io.github.shoaky.sourcedownloader.config.SourceDownloaderProperties
import io.github.shoaky.sourcedownloader.core.component.ComponentManager
import io.github.shoaky.sourcedownloader.sdk.InstanceManager
import io.github.shoaky.sourcedownloader.sdk.plugin.Plugin
import org.slf4j.LoggerFactory
import java.util.*

class PluginManager(
    componentManager: ComponentManager,
    instanceManager: InstanceManager,
    applicationProps: SourceDownloaderProperties
) {

    private val pluginContext = DefaultPluginContext(
        componentManager,
        instanceManager,
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

    companion object {

        private val log = LoggerFactory.getLogger(PluginManager::class.java)
    }
}