package io.github.shoaky.sourcedownloader.foreign

import io.github.shoaky.sourcedownloader.foreign.supplier.ForeignSourceSupplier
import io.github.shoaky.sourcedownloader.sdk.plugin.Plugin
import io.github.shoaky.sourcedownloader.sdk.plugin.PluginContext
import io.github.shoaky.sourcedownloader.sdk.plugin.PluginDescription

class ForeignPlugin : Plugin {

    override fun init(pluginContext: PluginContext) {
        pluginContext.registerSupplier(
            ForeignSourceSupplier
        )
    }

    override fun destroy(pluginContext: PluginContext) {
    }

    override fun description(): PluginDescription {
        return PluginDescription(
            "foreign-plugin",
            "0.0.1"
        )
    }
}