package xyz.shoaky.sourcedownloader.sdk

import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

interface PluginContext {

    fun registerSupplier(vararg suppliers: SdComponentSupplier<*>)

    fun addPatternVarsDescription(vararg descriptions: PatternVarsDescription)
}