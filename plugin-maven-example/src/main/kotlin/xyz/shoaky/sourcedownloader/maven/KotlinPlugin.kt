package xyz.shoaky.sourcedownloader.maven

import xyz.shoaky.sourcedownloader.sdk.Plugin
import xyz.shoaky.sourcedownloader.sdk.PluginContext
import xyz.shoaky.sourcedownloader.sdk.PluginDescription

class KotlinPlugin : Plugin {
    override fun init(pluginContext: PluginContext) {

    }

    override fun destroy(pluginContext: PluginContext) {

    }

    override fun description(): PluginDescription {
        return PluginDescription("KotlinPluginExample", "0.0.1")
    }

}