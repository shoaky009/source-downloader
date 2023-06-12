package io.github.shoaky.sourcedownloader.core

import io.github.shoaky.sourcedownloader.SourceDownloaderApplication.Companion.log
import io.github.shoaky.sourcedownloader.sdk.InstanceFactory
import io.github.shoaky.sourcedownloader.sdk.InstanceManager
import io.github.shoaky.sourcedownloader.sdk.Properties
import java.util.concurrent.ConcurrentHashMap

class DefaultInstanceManager(
    private val instanceConfigStorage: InstanceConfigStorage
) : InstanceManager {

    private val instanceFactories: MutableMap<Class<*>, InstanceFactory<*>> = ConcurrentHashMap()
    private val instances: MutableMap<String, Any> = ConcurrentHashMap()

    @Suppress("UNCHECKED_CAST")
    override fun <T> load(name: String, klass: Class<T>, props: Properties?): T {
        var createdFlag = false
        val instance = instances.computeIfAbsent(name) {
            val instanceFactory = instanceFactories[klass] as? InstanceFactory<T>
                ?: throw RuntimeException("no factory found for $klass")
            val instanceProps = props ?: instanceConfigStorage.getInstanceProps(name)
            val create = instanceFactory.create(instanceProps) ?: throw RuntimeException("create instance failed")
            log.info("成功创建实例$name")
            createdFlag = true
            create
        }

        if (createdFlag) {
            instances[name] = instance
        }
        instance as? T ?: throw RuntimeException("instance $name is not instance of $klass")
        return instance
    }

    override fun <T> getInstance(klass: Class<T>): List<T> {
        return instances.values.filterIsInstance(klass)
    }

    override fun registerInstanceFactory(vararg factories: InstanceFactory<*>) {
        for (factory in factories) {
            val type = factory.type()
            val instanceFactory = instanceFactories[type]
            if (instanceFactory != null) {
                throw RuntimeException("instance factory for $type already exists")
            }
            instanceFactories[type] = factory
        }
    }

    fun destroyAll() {
        instances.values.forEach {
            if (it is AutoCloseable) {
                it.close()
            }
        }
        instances.clear()
    }
}


interface InstanceConfigStorage {
    fun getInstanceProps(name: String): Properties
}