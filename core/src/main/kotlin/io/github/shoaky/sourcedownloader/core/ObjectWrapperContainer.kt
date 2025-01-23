package io.github.shoaky.sourcedownloader.core

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.core.component.ComponentWrapper
import io.github.shoaky.sourcedownloader.core.component.ObjectWrapper
import io.github.shoaky.sourcedownloader.core.component.ProcessorWrapper
import io.github.shoaky.sourcedownloader.sdk.SourcePointer
import io.github.shoaky.sourcedownloader.sdk.component.*

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
val sourceTypeRef = componentTypeRef<Source<SourcePointer>>()
val fileMoverTypeRef = componentTypeRef<FileMover>()
val downloaderTypeRef = componentTypeRef<Downloader>()
val fileResolverTypeRef = componentTypeRef<ItemFileResolver>()
val variableProviderTypeRef = componentTypeRef<VariableProvider>()
val sourceItemFilterTypeRef = componentTypeRef<SourceItemFilter>()
val sourceFileFilterTypeRef = componentTypeRef<SourceFileFilter>()
val fileContentFilterTypeRef = componentTypeRef<FileContentFilter>()
val itemContentFilterTypeRef = componentTypeRef<ItemContentFilter>()
val processListenerTypeRef = componentTypeRef<ProcessListener>()
val fileTaggerTypeRef = componentTypeRef<FileTagger>()
val fileReplacementDeciderRef = componentTypeRef<FileReplacementDecider>()
val fileExistsDetectorTypeRef = componentTypeRef<FileExistsDetector>()
val variableReplacerTypeRef = componentTypeRef<VariableReplacer>()
val manualSourceRef = componentTypeRef<ManualSource>()
val processorTypeRef = jacksonTypeRef<ProcessorWrapper>()

