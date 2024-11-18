package io.github.shoaky.sourcedownloader.core

import io.github.shoaky.sourcedownloader.sdk.plugin.Plugin

interface PluginLoader {

    fun loadPlugins(classLoader: ClassLoader? = null): List<Plugin>
}