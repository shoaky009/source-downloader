package io.github.shoaky.sourcedownloader.core

import io.github.shoaky.sourcedownloader.config.SourceDownloaderProperties
import io.github.shoaky.sourcedownloader.core.component.ComponentManager
import io.github.shoaky.sourcedownloader.sdk.InstanceFactory
import io.github.shoaky.sourcedownloader.sdk.InstanceManager
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.plugin.PluginContext
import java.nio.file.Path

class DefaultPluginContext(
    private val componentManager: ComponentManager,
    private val instanceManager: InstanceManager,
    // 这个不应该这样注入, 不过暂时没有其他应用级别的需求
    private val applicationProps: SourceDownloaderProperties
) : PluginContext {

    override fun getPersistentDataPath(): Path {
        return applicationProps.dataLocation
    }

    override fun registerSupplier(vararg suppliers: ComponentSupplier<*>) {
        componentManager.registerSupplier(*suppliers)
    }

    override fun registerInstanceFactory(vararg factories: InstanceFactory<*>) {
        instanceManager.registerInstanceFactory(*factories)
    }

    override fun <T> loadInstance(name: String, klass: Class<T>, props: Properties?): T {
        return instanceManager.loadInstance(name, klass, props)
    }

    override fun getInstanceManager(): InstanceManager {
        return instanceManager
    }
}