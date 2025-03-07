package io.github.shoaky.sourcedownloader.core.component

import io.github.shoaky.sourcedownloader.component.supplier.KeywordIntegrationSupplier
import io.github.shoaky.sourcedownloader.core.*
import io.github.shoaky.sourcedownloader.sdk.component.ComponentTopType
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.SourceItemFilter
import io.github.shoaky.sourcedownloader.sdk.component.VariableProvider
import io.github.shoaky.sourcedownloader.testResourcePath
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class DefaultComponentManagerTest {

    private val storage = YamlConfigOperator(testResourcePath.resolve("config.yaml"))

    @Test
    fun given_multi_types_decl_component_should_auto_register_others() {
        val container = SimpleObjectWrapperContainer()
        val storages = listOf(storage)
        val manager = DefaultComponentManager(container, storages)
        manager.registerSupplier(KeywordIntegrationSupplier)

        val providerWp = manager.getComponent(
            ComponentTopType.VARIABLE_PROVIDER,
            ComponentId("keyword"),
            variableProviderTypeRef
        )
        assertEquals(2, container.getAllObjectNames().size)

        val filterWp = manager.getComponent(
            ComponentTopType.SOURCE_ITEM_FILTER,
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
}