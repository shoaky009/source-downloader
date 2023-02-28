package xyz.shoaky.sourcedownloader.sdk

import xyz.shoaky.sourcedownloader.sdk.component.ComponentSupplier

interface PluginContext {

    fun registerSupplier(vararg suppliers: ComponentSupplier<*>)

    fun addPatternVarsDescription(vararg descriptions: PatternVarsDescription)
}