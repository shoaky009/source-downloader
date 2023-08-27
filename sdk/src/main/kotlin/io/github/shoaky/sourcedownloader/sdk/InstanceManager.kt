package io.github.shoaky.sourcedownloader.sdk

interface InstanceManager {

    fun <T> load(name: String, klass: Class<T>, props: Properties? = null): T

    fun <T> getInstance(klass: Class<T>): List<T>

    fun registerInstanceFactory(vararg factories: InstanceFactory<*>)
}