package xyz.shoaky.sourcedownloader.core

import xyz.shoaky.sourcedownloader.sdk.InstanceFactory
import xyz.shoaky.sourcedownloader.sdk.PluginContext
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.SdComponentSupplier

class DefaultPluginContext(
    private val componentManager: SdComponentManager,
    private val instanceManager: InstanceManager
) : PluginContext {
    override fun registerSupplier(vararg suppliers: SdComponentSupplier<*>) {
        componentManager.registerSupplier(*suppliers)
    }

    override fun registerInstanceFactory(vararg factories: InstanceFactory<*>) {
        instanceManager.registerInstanceFactory(*factories)
    }

    override fun <T> load(name: String, klass: Class<T>, props: Properties?): T {
        return instanceManager.load(name, klass, props)
    }

    override fun <T> getInstances(klass: Class<T>): List<T> {
        return instanceManager.getInstance(klass)
    }

}