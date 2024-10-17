package io.github.shoaky.sourcedownloader.application.spring.config

import io.github.shoaky.sourcedownloader.CoreApplication
import io.github.shoaky.sourcedownloader.application.spring.SpringSourceDownloaderProperties
import io.github.shoaky.sourcedownloader.application.spring.component.SpringWebFrameworkAdapter
import io.github.shoaky.sourcedownloader.application.spring.converter.ComponentsConverter
import io.github.shoaky.sourcedownloader.component.supplier.WebhookTriggerSupplier
import io.github.shoaky.sourcedownloader.config.SourceDownloaderProperties
import io.github.shoaky.sourcedownloader.core.*
import io.github.shoaky.sourcedownloader.core.component.*
import io.github.shoaky.sourcedownloader.core.processor.DefaultProcessorManager
import io.github.shoaky.sourcedownloader.core.processor.ProcessorManager
import io.github.shoaky.sourcedownloader.repo.exposed.ExposedProcessingStorage
import io.github.shoaky.sourcedownloader.sdk.InstanceManager
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.service.ComponentService
import io.github.shoaky.sourcedownloader.service.ProcessingContentService
import io.github.shoaky.sourcedownloader.service.ProcessorService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

@Configuration
class ApplicationConfiguration {

    @Bean
    fun props(props: SpringSourceDownloaderProperties): SourceDownloaderProperties {
        return SourceDownloaderProperties(props.dataLocation)
    }
    @Bean
    fun coreApplication(
        sourceDownloaderProperties: SourceDownloaderProperties,
        instanceManager: InstanceManager,
        componentManager: ComponentManager,
        processorManager: ProcessorManager,
        pluginManager: PluginManager,
        processorStorages: List<ProcessorConfigStorage>,
        componentSupplier: List<ComponentSupplier<*>>
    ): CoreApplication {
        return CoreApplication(
            sourceDownloaderProperties,
            instanceManager,
            componentManager,
            processorManager,
            pluginManager,
            processorStorages,
            componentSupplier
        )
    }

    @Bean
    fun objectContainer(): ObjectWrapperContainer {
        return SimpleObjectWrapperContainer()
    }

    @Bean
    fun instanceManager(storage: InstanceConfigStorage): InstanceManager {
        return DefaultInstanceManager(storage)
    }

    @Bean(destroyMethod = "destroy")
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

    @Bean
    fun componentService(
        componentManager: ComponentManager,
        configOperator: ConfigOperator,
    ): ComponentService {
        return ComponentService(componentManager, configOperator)
    }

    @Bean
    fun processorService(
        processorManager: ProcessorManager,
        configOperator: ConfigOperator,
        processingStorage: ProcessingStorage
    ): ProcessorService {
        return ProcessorService(
            processorManager, configOperator, processingStorage
        )
    }

    @Bean
    fun processingContentService(
        storage: ProcessingStorage,
        processorManager: ProcessorManager
    ): ProcessingContentService {
        return ProcessingContentService(storage, processorManager)
    }

    @Bean
    fun defaultComponents(): ComponentConfigStorage {
        return DefaultComponents()
    }

    @Bean
    fun processingStorage(): ProcessingStorage {
        return ExposedProcessingStorage()
    }

    @Bean
    fun webhookTriggerSupplier(
        @Qualifier("requestMappingHandlerMapping")
        requestMapping: RequestMappingHandlerMapping
    ): WebhookTriggerSupplier {
        return WebhookTriggerSupplier(SpringWebFrameworkAdapter(requestMapping))
    }
}