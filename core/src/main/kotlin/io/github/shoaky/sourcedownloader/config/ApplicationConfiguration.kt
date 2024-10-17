package io.github.shoaky.sourcedownloader.config

import io.github.shoaky.sourcedownloader.core.ObjectWrapperContainer
import io.github.shoaky.sourcedownloader.core.PluginManager
import io.github.shoaky.sourcedownloader.core.ProcessingStorage
import io.github.shoaky.sourcedownloader.core.SimpleObjectWrapperContainer
import io.github.shoaky.sourcedownloader.core.component.*
import io.github.shoaky.sourcedownloader.core.processor.DefaultProcessorManager
import io.github.shoaky.sourcedownloader.core.processor.ProcessorManager
import io.github.shoaky.sourcedownloader.sdk.InstanceManager
import io.github.shoaky.sourcedownloader.util.converter.ComponentsConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ApplicationConfiguration {

    // @Bean
    // fun objectContainer(applicationContext: DefaultListableBeanFactory): ObjectWrapperContainer {
    //     return SpringObjectWrapperContainer(applicationContext)
    // }

    @Bean
    fun objectContainer(): ObjectWrapperContainer {
        return SimpleObjectWrapperContainer()
    }

    @Bean
    fun instanceManager(storage: InstanceConfigStorage): InstanceManager {
        return DefaultInstanceManager(storage)
    }

    @Bean
    fun componentManager(
        objectWrapperContainer: ObjectWrapperContainer,
        configStorages: List<ComponentConfigStorage>
    ): ComponentManager {
        return DefaultComponentManager(objectWrapperContainer, configStorages)
    }

    @Bean
    fun processorManager(
        processingStorage: ProcessingStorage,
        componentManager: ComponentManager,
        objectWrapperContainer: ObjectWrapperContainer,
    ): ProcessorManager {
        return DefaultProcessorManager(processingStorage, componentManager, objectWrapperContainer)
    }

    @Bean
    fun pluginManager(
        componentManager: ComponentManager,
        instanceManager: InstanceManager,
        properties: SourceDownloaderProperties
    ): PluginManager {
        return PluginManager(
            componentManager,
            instanceManager,
            properties
        )
    }

    @Bean
    fun componentsConverter(): ComponentsConverter {
        return ComponentsConverter()
    }

}