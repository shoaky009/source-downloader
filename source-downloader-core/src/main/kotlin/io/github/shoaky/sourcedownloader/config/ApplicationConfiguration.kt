package io.github.shoaky.sourcedownloader.config

import io.github.shoaky.sourcedownloader.core.ObjectContainer
import io.github.shoaky.sourcedownloader.core.ProcessingStorage
import io.github.shoaky.sourcedownloader.core.SpringObjectContainer
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
    fun objectContainer(applicationContext: DefaultListableBeanFactory): ObjectContainer {
        return SpringObjectContainer(applicationContext)
    }

    @Bean
    fun componentManager(objectContainer: ObjectContainer): ComponentManager {
        return DefaultComponentManager(objectContainer)
    }

    @Bean
    fun processorManager(
        processingStorage: ProcessingStorage,
        componentManager: ComponentManager,
        objectContainer: ObjectContainer,
    ): ProcessorManager {
        return DefaultProcessorManager(processingStorage, componentManager, objectContainer)
    }


}