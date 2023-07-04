package io.github.shoaky.sourcedownloader.core

import org.springframework.beans.factory.support.DefaultListableBeanFactory

interface ObjectContainer {

    fun contains(name: String): Boolean

    fun put(name: String, value: Any)

    fun get(name: String): Any {
        return get(name, Any::class.java)
    }

    fun <T> get(name: String, type: Class<T>): T

    fun <T> getObjectsOfType(type: Class<T>): Map<String, T>

    fun remove(name: String)

    fun getAllObjectNames(): Set<String>

}

class SpringObjectContainer(
    private val applicationContext: DefaultListableBeanFactory
) : ObjectContainer {

    override fun contains(name: String): Boolean {
        return applicationContext.containsBean(name)
    }

    override fun put(name: String, value: Any) {
        applicationContext.registerSingleton(name, value)
    }

    override fun <T> get(name: String, type: Class<T>): T {
        return applicationContext.getBean(name, type)
    }

    override fun <T> getObjectsOfType(type: Class<T>): Map<String, T> {
        return applicationContext.getBeansOfType(type)
    }

    override fun remove(name: String) {
        applicationContext.destroySingleton(name)
    }

    override fun getAllObjectNames(): Set<String> {
        return applicationContext.singletonNames.toSet()
    }

}