package io.github.shoaky.sourcedownloader.core.processor

import io.github.shoaky.sourcedownloader.CoreApplication
import io.github.shoaky.sourcedownloader.core.MemoryConfigOperator
import io.github.shoaky.sourcedownloader.core.NoProcessingStorage
import io.github.shoaky.sourcedownloader.core.ProcessorConfig
import io.github.shoaky.sourcedownloader.core.SimpleObjectWrapperContainer
import io.github.shoaky.sourcedownloader.core.component.ComponentConfig
import io.github.shoaky.sourcedownloader.core.component.ComponentId
import io.github.shoaky.sourcedownloader.core.component.DefaultComponentManager
import io.github.shoaky.sourcedownloader.sdk.component.ComponentRootType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class DefaultProcessorManagerTest {

    @Test
    fun test_error_whole_lifecycle() {
        val operator = MemoryConfigOperator()
        val container = SimpleObjectWrapperContainer()
        val componentManager = DefaultComponentManager(container, listOf(operator))
        componentManager.registerSupplier(
            *CoreApplication.coreSupplierBundle()
        )
        val processorManager = DefaultProcessorManager(
            NoProcessingStorage,
            componentManager,
            container
        )

        val processorName = "test"
        val config = ProcessorConfig(
            processorName,
            emptyList(),
            ComponentId("fixed:test"),
            ComponentId("fixed:test"),
            ComponentId("none"),
            ComponentId("general"),
            "",
        )
        processorManager.createProcessor(
            config
        )
        val pWrapper = processorManager.getProcessor("test")
        assert(pWrapper.errorMessage != null)
        assert(pWrapper.processor == null)

        // simulate after redefine
        operator.save(
            ComponentRootType.SOURCE.primaryName,
            ComponentConfig("test", "fixed", mapOf("content" to emptyList<Any>()))
        )
        operator.save(ComponentRootType.ITEM_FILE_RESOLVER.primaryName, ComponentConfig("test", "fixed"))

        processorManager.destroyProcessor(processorName)
        processorManager.createProcessor(config)
        val pw = processorManager.getProcessor(processorName)
        assert(pw.errorMessage == null)
        assert(pw.processor != null)
        assertDoesNotThrow { pw.get() }
    }
}