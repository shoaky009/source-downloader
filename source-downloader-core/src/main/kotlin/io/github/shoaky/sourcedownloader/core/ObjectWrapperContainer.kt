package io.github.shoaky.sourcedownloader.core

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.core.component.ComponentWrapper
import io.github.shoaky.sourcedownloader.core.component.ObjectWrapper
import io.github.shoaky.sourcedownloader.core.component.ProcessorWrapper
import io.github.shoaky.sourcedownloader.sdk.SourceItemPointer
import io.github.shoaky.sourcedownloader.sdk.component.*
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.core.ResolvableType

interface ObjectWrapperContainer {

    fun contains(name: String): Boolean

    fun put(name: String, value: ObjectWrapper<*>)

    fun get(name: String): ObjectWrapper<Any> {
        return get(name, jacksonTypeRef())
    }

    fun <T : Any, W : ObjectWrapper<T>> get(name: String, type: TypeReference<W>): W

    fun <T : Any, W : ObjectWrapper<T>> getObjectsOfType(type: TypeReference<W>): Map<String, W>

    fun remove(name: String)

    fun getAllObjectNames(): Set<String>

}

inline fun <reified T : SdComponent> componentTypeRef(): TypeReference<ComponentWrapper<T>> =
    object : TypeReference<ComponentWrapper<T>>() {}

val triggerTypeRef = componentTypeRef<Trigger>()
val sourceTypeRef = componentTypeRef<Source<SourceItemPointer>>()
val fileMoverTypeRef = componentTypeRef<FileMover>()
val downloaderTypeRef = componentTypeRef<Downloader>()
val fileResolverTypeRef = componentTypeRef<ItemFileResolver>()
val variableProviderTypeRef = componentTypeRef<VariableProvider>()
val fileContentFilterTypeRef = componentTypeRef<FileContentFilter>()
val runAfterCompletionTypeRef = componentTypeRef<RunAfterCompletion>()
val fileTaggerTypeRef = componentTypeRef<FileTagger>()
val processorTypeRef = jacksonTypeRef<ProcessorWrapper>()

class SpringObjectWrapperContainer(
    private val applicationContext: DefaultListableBeanFactory,
) : ObjectWrapperContainer {

    override fun contains(name: String): Boolean {
        return applicationContext.containsBean(name)
    }

    override fun put(name: String, value: ObjectWrapper<*>) {
        applicationContext.registerSingleton(name, value)
    }

    override fun <T : Any, W : ObjectWrapper<T>> get(name: String, type: TypeReference<W>): W {
        @Suppress("UNCHECKED_CAST")
        return applicationContext.getBean(name) as? W
            ?: throw IllegalArgumentException("Bean $name cannot be cast to ${type.type}")
    }

    override fun <T : Any, W : ObjectWrapper<T>> getObjectsOfType(type: TypeReference<W>): Map<String, W> {
        val names = applicationContext.getBeanNamesForType(ResolvableType.forType(type.type))
        @Suppress("UNCHECKED_CAST")
        return applicationContext.getBeansOfType(ResolvableType.forType(type.type).resolve()) as? Map<String, W>
            ?: throw IllegalArgumentException("Bean $names cannot be cast to ${type.type}")
    }

    override fun remove(name: String) {
        applicationContext.destroySingleton(name)
    }

    override fun getAllObjectNames(): Set<String> {
        return applicationContext.singletonNames.toSet()
    }

}