package io.github.shoaky.sourcedownloader.core.component

import io.github.shoaky.sourcedownloader.component.supplier.KeywordIntegrationSupplier
import io.github.shoaky.sourcedownloader.component.supplier.RegexVariableProviderSupplier
import io.github.shoaky.sourcedownloader.core.*
import io.github.shoaky.sourcedownloader.sdk.component.ComponentRootType
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.SourceItemFilter
import io.github.shoaky.sourcedownloader.sdk.component.VariableProvider
import io.github.shoaky.sourcedownloader.testResourcePath
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class DefaultComponentManagerTest {

    private val defaultStorage = YamlConfigOperator(testResourcePath.resolve("config.yaml"))

    @Test
    fun given_multi_types_decl_component_should_auto_register_others() {
        val container = SimpleObjectWrapperContainer()
        val storages = listOf(defaultStorage)
        val manager = DefaultComponentManager(container, storages)
        manager.registerSupplier(KeywordIntegrationSupplier)

        val providerWp = manager.getComponent(
            ComponentRootType.VARIABLE_PROVIDER,
            ComponentId("keyword"),
            variableProviderTypeRef
        )
        assertEquals(2, container.getAllObjectNames().size)

        val filterWp = manager.getComponent(
            ComponentRootType.SOURCE_ITEM_FILTER,
            ComponentId("keyword"),
            sourceItemFilterTypeRef
        )

        assert(providerWp.primary)
        assert(!filterWp.primary)
        assert(filterWp.get() === providerWp.get())

        // also check delegate
        filterWp.getAndMarkRef("11", SourceItemFilter::class)
        val component = providerWp.getAndMarkRef("11", VariableProvider::class)
        assert(component is CachedVariableProvider)

        manager.destroy(
            ComponentType.variableProvider("keyword"), "keyword"
        )

        assertEquals(0, container.getAllObjectNames().size)
    }

    @Test
    fun given_error_component_test_whole_lifecycle() {
        val configStorage = MemoryConfigOperator()
        configStorage.save(
            ComponentRootType.VARIABLE_PROVIDER.primaryName,
            ComponentConfig("error-props", "regex")
        )

        val container = SimpleObjectWrapperContainer()
        val storages = listOf(configStorage)
        val manager = DefaultComponentManager(container, storages)
        manager.registerSupplier(RegexVariableProviderSupplier)

        val providerWp = manager.getComponent(
            ComponentRootType.VARIABLE_PROVIDER,
            ComponentId("regex:error-props"),
            variableProviderTypeRef
        )
        val errorMessage = providerWp.errorMessage
        assert(errorMessage != null)
        assert(providerWp.component == null)
        assertThrows<RuntimeException> {
            providerWp.get()
        }

        // test reload lifecycle
        configStorage.save(
            ComponentRootType.VARIABLE_PROVIDER.primaryName,
            ComponentConfig(
                "error-props", "regex", mapOf(
                    "regexes" to listOf(
                        mapOf(
                            "name" to "aaa",
                            "regex" to "bbb"
                        )
                    )
                )
            )
        )

        manager.destroy(ComponentType.variableProvider("regex"), "error-props")
        val providerWp2 = manager.getComponent(
            ComponentRootType.VARIABLE_PROVIDER,
            ComponentId("regex:error-props"),
            variableProviderTypeRef
        )
        assert(providerWp2.errorMessage == null)
        assertDoesNotThrow { providerWp2.get() }
    }
}