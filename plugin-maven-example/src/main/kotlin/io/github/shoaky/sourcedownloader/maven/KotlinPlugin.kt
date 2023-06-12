package io.github.shoaky.sourcedownloader.maven

import io.github.shoaky.sourcedownloader.sdk.Plugin
import io.github.shoaky.sourcedownloader.sdk.PluginContext
import io.github.shoaky.sourcedownloader.sdk.PluginDescription

class KotlinPlugin : Plugin {
    override fun init(pluginContext: PluginContext) {

    }

    override fun destroy(pluginContext: PluginContext) {

    }

    override fun description(): PluginDescription {
        return PluginDescription("KotlinPluginExample", "0.0.1")
    }

}