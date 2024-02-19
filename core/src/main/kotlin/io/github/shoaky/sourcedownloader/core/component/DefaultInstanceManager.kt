package io.github.shoaky.sourcedownloader.core.component

import io.github.shoaky.sourcedownloader.sdk.InstanceFactory
import io.github.shoaky.sourcedownloader.sdk.InstanceManager
import io.github.shoaky.sourcedownloader.sdk.Properties
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

class DefaultInstanceManager(
    private val instanceConfigStorage: InstanceConfigStorage
) : InstanceManager {

    private val instanceFactories: MutableMap<Class<*>, InstanceFactory<*>> = ConcurrentHashMap()
    private val instances: MutableMap<String, Any> = ConcurrentHashMap()

    @Suppress("UNCHECKED_CAST")
    override fun <T> loadInstance(name: String, klass: Class<T>, props: Properties?): T {
        var createdFlag = false
        val instance = instances.computeIfAbsent(name) {
            val instanceFactory = instanceFactories[klass] as? InstanceFactory<T>
                ?: throw RuntimeException("No factory found for $klass")
            val instanceProps = props ?: instanceConfigStorage.getInstanceProps(name)
            val created = instanceFactory.create(instanceProps) ?: throw RuntimeException("Create instance failed")
            log.info("Successfully created instance $name")
            createdFlag = true
            created
        }

        if (createdFlag) {
            instances[name] = instance
        }
        instance as? T ?: throw RuntimeException("Instance $name is not instance of $klass")
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

    override fun destroyInstance(name: String) {
        instances.remove(name)?.run {
            if (this is AutoCloseable) {
                this.close()
            }
        }
    }

    fun destroyAll() {
        instances.keys.forEach {
            destroyInstance(it)
        }
    }

    companion object {

        private val log = LoggerFactory.getLogger("InstanceManager")
    }
}

interface InstanceConfigStorage {

    fun getInstanceProps(name: String): Properties
}