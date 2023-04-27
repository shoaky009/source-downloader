package xyz.shoaky.sourcedownloader.core

import xyz.shoaky.sourcedownloader.sdk.InstanceFactory
import xyz.shoaky.sourcedownloader.sdk.Properties

interface InstanceManager {

    fun <T> load(name: String, klass: Class<T>, props: Properties? = null): T

    fun <T> getInstance(klass: Class<T>): List<T>

    fun registerInstanceFactory(vararg factories: InstanceFactory<*>)
}