package xyz.shoaky.sourcedownloader.core

import org.springframework.core.io.support.SpringFactoriesLoader
import org.springframework.stereotype.Component
import xyz.shoaky.sourcedownloader.sdk.Plugin
import java.util.*

@Component
class PluginManager(componentManager: SdComponentManager) {

    private val pluginContext = DefaultPluginContext(componentManager)
    private val plugins = Collections.synchronizedList(mutableListOf<Plugin>())
    private val pluginLoader = ServiceLoaderPluginLoader
    fun loadPlugins() {
        val loadedPlugins = pluginLoader.loadPlugins()
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

    //NOTE native image下有点问题，暂时不用
    private object SpringPluginLoader : PluginLoader {
        override fun loadPlugins(classLoader: ClassLoader?): List<Plugin> {
            val loader = SpringFactoriesLoader.forResourceLocation("META-INF/plugin", classLoader)
            return loader.load(Plugin::class.java)
        }
    }

    private object ServiceLoaderPluginLoader : PluginLoader {
        override fun loadPlugins(classLoader: ClassLoader?): List<Plugin> {
            return ServiceLoader.load(Plugin::class.java, classLoader).map { it }
        }
    }
}

interface PluginLoader {
    fun loadPlugins(classLoader: ClassLoader? = null): List<Plugin>
}
