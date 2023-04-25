package xyz.shoaky.sourcedownloader.ai

import xyz.shoaky.sourcedownloader.sdk.Plugin
import xyz.shoaky.sourcedownloader.sdk.PluginContext
import xyz.shoaky.sourcedownloader.sdk.PluginDescription

internal class AiPlugin : Plugin {
    override fun init(pluginContext: PluginContext) {
        pluginContext.registerSupplier(
            OpenaiVariableProviderSupplier
        )
    }

    override fun destroy(pluginContext: PluginContext) {

    }

    override fun description(): PluginDescription {
        return PluginDescription("ai", "0.0.1")
    }
}