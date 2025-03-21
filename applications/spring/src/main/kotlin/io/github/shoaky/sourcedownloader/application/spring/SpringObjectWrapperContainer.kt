package io.github.shoaky.sourcedownloader.application.spring

import com.fasterxml.jackson.core.type.TypeReference
import io.github.shoaky.sourcedownloader.core.ObjectWrapperContainer
import io.github.shoaky.sourcedownloader.core.component.ComponentFailureType
import io.github.shoaky.sourcedownloader.core.component.ObjectWrapper
import io.github.shoaky.sourcedownloader.throwComponentException
import org.springframework.beans.BeansException
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.core.ResolvableType

class SpringObjectWrapperContainer(
    private val applicationContext: DefaultListableBeanFactory,
) : ObjectWrapperContainer {

    override fun contains(name: String): Boolean {
        return applicationContext.containsBean(name)
    }

    override fun put(name: String, value: ObjectWrapper<*>) {
        applicationContext.registerSingleton(name, value)
    }

    override fun <T : Any, W : ObjectWrapper<T>> get(name: String, typeRef: TypeReference<W>): W {
        @Suppress("UNCHECKED_CAST")
        return try {
            applicationContext.getBean(name) as? W
                ?: throw IllegalArgumentException("Bean $name cannot be cast to ${typeRef.type}")
        } catch (e: BeansException) {
            throwComponentException("No bean named $name available", ComponentFailureType.INSTANCE_NOT_FOUND)
        }
    }

    override fun <T : Any, W : ObjectWrapper<T>> getObjectsOfType(typeRef: TypeReference<W>): Map<String, W> {
        val names = applicationContext.getBeanNamesForType(ResolvableType.forType(typeRef.type))
        @Suppress("UNCHECKED_CAST")
        return applicationContext.getBeansOfType(ResolvableType.forType(typeRef.type).resolve()) as? Map<String, W>
            ?: throw IllegalArgumentException("Bean $names cannot be cast to ${typeRef.type}")
    }

    override fun remove(name: String) {
        applicationContext.destroySingleton(name)
    }

    override fun getAllObjectNames(): Set<String> {
        return applicationContext.singletonNames.toSet()
    }

}