package io.github.shoaky.sourcedownloader.config

import io.github.shoaky.sourcedownloader.core.ObjectWrapperContainer
import io.github.shoaky.sourcedownloader.core.ProcessingStorage
import io.github.shoaky.sourcedownloader.core.SpringObjectWrapperContainer
import io.github.shoaky.sourcedownloader.core.component.ComponentManager
import io.github.shoaky.sourcedownloader.core.component.DefaultComponentManager
import io.github.shoaky.sourcedownloader.core.processor.DefaultProcessorManager
import io.github.shoaky.sourcedownloader.core.processor.ProcessorManager
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ApplicationConfiguration {

    @Bean
    fun objectContainer(applicationContext: DefaultListableBeanFactory): ObjectWrapperContainer {
        return SpringObjectWrapperContainer(applicationContext)
    }

    @Bean
    fun componentManager(objectWrapperContainer: ObjectWrapperContainer): ComponentManager {
        return DefaultComponentManager(objectWrapperContainer)
    }

    @Bean
    fun processorManager(
        processingStorage: ProcessingStorage,
        componentManager: ComponentManager,
        objectWrapperContainer: ObjectWrapperContainer,
    ): ProcessorManager {
        return DefaultProcessorManager(processingStorage, componentManager, objectWrapperContainer)
    }


}