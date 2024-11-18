package io.github.shoaky.sourcedownloader.core.component

import com.fasterxml.jackson.core.type.TypeReference
import io.github.shoaky.sourcedownloader.core.processor.SourceProcessor
import io.github.shoaky.sourcedownloader.sdk.SourcePointer
import io.github.shoaky.sourcedownloader.sdk.component.*

/**
 * Component instance manager
 */
interface ComponentManager {

    fun <T : SdComponent> getComponent(
        type: ComponentTopType,
        id: ComponentId,
        typeReference: TypeReference<ComponentWrapper<T>>,
    ): ComponentWrapper<T>

    fun getAllProcessor(): List<SourceProcessor>

    fun getComponent(type: ComponentType, name: String): ComponentWrapper<SdComponent>?

    fun getAllComponent(): List<ComponentWrapper<SdComponent>>

    fun getSupplier(type: ComponentType): ComponentSupplier<*>

    fun getSuppliers(): List<ComponentSupplier<*>>

    fun registerSupplier(vararg componentSuppliers: ComponentSupplier<*>)

    fun getAllComponentNames(): Set<String>

    fun getAllTrigger(): List<ComponentWrapper<Trigger>> {
        return getAllComponent()
            .filter { it.type.type == ComponentTopType.TRIGGER }
            .filterIsInstance<ComponentWrapper<Trigger>>()
    }

    fun getAllSource(): List<Source<SourcePointer>> {
        return getAllComponent().map { it.component }.filterIsInstance<Source<SourcePointer>>()
    }

    fun getAllDownloader(): List<Downloader> {
        return getAllComponent().map { it.component }.filterIsInstance<Downloader>()
    }

    fun getAllMover(): List<FileMover> {
        return getAllComponent().map { it.component }.filterIsInstance<FileMover>()
    }

    fun getAllProvider(): List<VariableProvider> {
        return getAllComponent().map { it.component }.filterIsInstance<VariableProvider>()
    }

    fun getAllManualSource(): List<ComponentWrapper<ManualSource>> {
        return getAllComponent().filter { it.type.type == ComponentTopType.MANUAL_SOURCE }
            .filterIsInstance<ComponentWrapper<ManualSource>>()
    }

    fun destroy(type: ComponentType, name: String)

    fun destroy()
}