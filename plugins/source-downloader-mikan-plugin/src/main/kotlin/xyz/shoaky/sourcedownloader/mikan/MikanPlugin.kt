package xyz.shoaky.sourcedownloader.mikan

import xyz.shoaky.sourcedownloader.sdk.Plugin
import xyz.shoaky.sourcedownloader.sdk.PluginContext
import xyz.shoaky.sourcedownloader.sdk.PluginDescription

class MikanPlugin : Plugin {
    override fun init(pluginContext: PluginContext) {
        // pluginContext.registerSupplier(MikanVariableProviderSupplier)
    }

    override fun destroy(pluginContext: PluginContext) {
        println("Mikan plugin destroy")
    }

    override fun description(): PluginDescription {
        return PluginDescription("Mikan", "1.0.0")
    }

}