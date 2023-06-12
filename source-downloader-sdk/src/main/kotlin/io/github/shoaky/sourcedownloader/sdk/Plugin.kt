package io.github.shoaky.sourcedownloader.sdk

interface Plugin {

    fun init(pluginContext: PluginContext)

    fun destroy(pluginContext: PluginContext)

    fun description(): PluginDescription
}