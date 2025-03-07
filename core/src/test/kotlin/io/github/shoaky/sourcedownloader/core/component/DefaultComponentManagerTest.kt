package io.github.shoaky.sourcedownloader.core.component

import io.github.shoaky.sourcedownloader.component.supplier.KeywordIntegrationSupplier
import io.github.shoaky.sourcedownloader.core.SimpleObjectWrapperContainer
import io.github.shoaky.sourcedownloader.core.YamlConfigOperator
import io.github.shoaky.sourcedownloader.core.sourceFileFilterTypeRef
import io.github.shoaky.sourcedownloader.core.variableProviderTypeRef
import io.github.shoaky.sourcedownloader.sdk.component.ComponentTopType
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.testResourcePath
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class DefaultComponentManagerTest {

    @Test
    fun given_multi_types_decl_component_should_auto_register_others() {
        val container = SimpleObjectWrapperContainer()
        val storages = listOf(
            YamlConfigOperator(testResourcePath.resolve("config.yaml"))
        )
        val manager = DefaultComponentManager(container, storages)
        manager.registerSupplier(KeywordIntegrationSupplier)

        val provider = manager.getComponent(
            ComponentTopType.VARIABLE_PROVIDER,
            ComponentId("keyword"),
            variableProviderTypeRef
        )

        val filter = manager.getComponent(
            ComponentTopType.SOURCE_ITEM_FILTER,
            ComponentId("keyword"),
            sourceFileFilterTypeRef
        )
        assert(provider.primary)
        assert(!filter.primary)
        assert(filter.get() === provider.get())
        assertEquals(2, container.getAllObjectNames().size)

        manager.destroy(
            ComponentType.variableProvider("keyword"), "keyword"
        )

        assertEquals(0, container.getAllObjectNames().size)
    }
}