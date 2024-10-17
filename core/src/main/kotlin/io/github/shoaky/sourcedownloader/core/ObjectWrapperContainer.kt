package io.github.shoaky.sourcedownloader.core

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.core.component.ComponentWrapper
import io.github.shoaky.sourcedownloader.core.component.ObjectWrapper
import io.github.shoaky.sourcedownloader.core.component.ProcessorWrapper
import io.github.shoaky.sourcedownloader.sdk.SourcePointer
import io.github.shoaky.sourcedownloader.sdk.component.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.concurrent.ConcurrentHashMap

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

class SimpleObjectWrapperContainer : ObjectWrapperContainer {

    private val objects: MutableMap<String, ObjectWrapper<*>> = ConcurrentHashMap()

    override fun contains(name: String): Boolean {
        return objects.containsKey(name)
    }

    override fun put(name: String, value: ObjectWrapper<*>) {
        objects[name] = value
    }

    override fun <T : Any, W : ObjectWrapper<T>> get(name: String, type: TypeReference<W>): W {
        @Suppress("UNCHECKED_CAST")
        return objects[name] as? W
            ?: throw IllegalArgumentException("Object $name cannot be cast to ${type.type}")
    }

    override fun <T : Any, W : ObjectWrapper<T>> getObjectsOfType(typeRef: TypeReference<W>): Map<String, W> {
        val rawType = typeRef.type
        var actualTypeArguments: Array<out Type>? = null
        if (rawType is ParameterizedType) {
            actualTypeArguments = rawType.actualTypeArguments
        }

        @Suppress("UNCHECKED_CAST")
        return objects.filterValues {
            if (actualTypeArguments == null) {
                return@filterValues it::class.java == rawType
            }
            if (actualTypeArguments.size > 1) {
                throw NotImplementedError("Multiple type arguments not implemented")
            }
            if (actualTypeArguments.size == 1 && it::class.java == (typeRef.type as ParameterizedType).rawType) {
                return@filterValues (actualTypeArguments[0] as Class<*>).isInstance(it.get())
            }
            return@filterValues false
        } as Map<String, W>
    }

    override fun remove(name: String) {
        objects.remove(name)
    }

    override fun getAllObjectNames(): Set<String> {
        return objects.keys
    }

}

inline fun <reified T : SdComponent> componentTypeRef(): TypeReference<ComponentWrapper<T>> =
    object : TypeReference<ComponentWrapper<T>>() {}

val triggerTypeRef = componentTypeRef<Trigger>()
val sourceTypeRef = componentTypeRef<Source<SourcePointer>>()
val fileMoverTypeRef = componentTypeRef<FileMover>()
val downloaderTypeRef = componentTypeRef<Downloader>()
val fileResolverTypeRef = componentTypeRef<ItemFileResolver>()
val variableProviderTypeRef = componentTypeRef<VariableProvider>()
val sourceItemFilterTypeRef = componentTypeRef<SourceItemFilter>()
val fileContentFilterTypeRef = componentTypeRef<FileContentFilter>()
val itemContentFilterTypeRef = componentTypeRef<ItemContentFilter>()
val processListenerTypeRef = componentTypeRef<ProcessListener>()
val fileTaggerTypeRef = componentTypeRef<FileTagger>()
val fileReplacementDeciderRef = componentTypeRef<FileReplacementDecider>()
val fileExistsDetectorTypeRef = componentTypeRef<FileExistsDetector>()
val variableReplacerTypeRef = componentTypeRef<VariableReplacer>()
val manualSourceRef = componentTypeRef<ManualSource>()
val processorTypeRef = jacksonTypeRef<ProcessorWrapper>()

