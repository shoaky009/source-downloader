package xyz.shoaky.sourcedownloader.core

import xyz.shoaky.sourcedownloader.sdk.PatternVarsDescription
import xyz.shoaky.sourcedownloader.sdk.PluginContext
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier
import xyz.shoaky.sourcedownloader.sdk.component.VariableProvider

class DefaultPluginContext(
    private val componentManager: ComponentManager
) : PluginContext {

    private val descriptionStorage = mutableMapOf<ComponentType, PatternVarsDescription>()
    override fun registerSupplier(vararg suppliers: SdComponentSupplier<*>) {
        componentManager.registerSupplier(*suppliers)
    }

    override fun addPatternVarsDescription(vararg descriptions: PatternVarsDescription) {
        for (desc in descriptions) {
            if (desc.componentType.klass != VariableProvider::class) {
                throw IllegalArgumentException("componentType must be VariableProvider::class")
            }
            val curr = descriptionStorage[desc.componentType]
            if (curr != null) {
                throw RuntimeException("duplicate componentType: ${desc.componentType}")
            }
            descriptionStorage[desc.componentType] = desc
        }
    }

}