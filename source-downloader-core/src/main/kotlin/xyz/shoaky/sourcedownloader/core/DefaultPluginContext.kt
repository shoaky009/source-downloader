package xyz.shoaky.sourcedownloader.core

import xyz.shoaky.sourcedownloader.sdk.PluginContext
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

class DefaultPluginContext(
    private val componentManager: SdComponentManager
) : PluginContext {
    override fun registerSupplier(vararg suppliers: SdComponentSupplier<*>) {
        componentManager.registerSupplier(*suppliers)
    }

}