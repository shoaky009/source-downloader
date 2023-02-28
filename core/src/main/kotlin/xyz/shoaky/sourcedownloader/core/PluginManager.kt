package xyz.shoaky.sourcedownloader.core

import org.springframework.core.io.support.SpringFactoriesLoader
import org.springframework.stereotype.Component
import xyz.shoaky.sourcedownloader.sdk.Plugin
import java.util.*

@Component
class PluginManager(componentManager: ComponentManager) {

    private val pluginContext = DefaultPluginContext(componentManager)
    private val plugins = Collections.synchronizedList(mutableListOf<Plugin>())
    fun loadPlugins() {
        val load = pluginLoader.load(Plugin::class.java)
        plugins.addAll(load)
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

    companion object {
        private val pluginLoader: SpringFactoriesLoader = SpringFactoriesLoader.forResourceLocation("META-INF/plugin.factories")
    }
}