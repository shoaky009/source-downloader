package io.github.shoaky.sourcedownloader.core

import io.github.shoaky.sourcedownloader.component.source.SystemFileSource
import io.github.shoaky.sourcedownloader.core.component.ComponentWrapper
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import org.junit.jupiter.api.Test

class SimpleObjectWrapperContainerTest {

    @Test
    fun given_multi_type_component_should_expect_same_object() {
        val container = SimpleObjectWrapperContainer()
        val name = "1"
        val wrapper = ComponentWrapper(
            ComponentType.downloader("system-file"),
            name,
            Properties.empty,
            null,
            true,
            SystemFileSource::class.java,
            null
        )
        container.put(name, wrapper)

        val dObjects = container.getObjectsOfType(downloaderTypeRef)
        assert(dObjects.isNotEmpty())
        assert(dObjects[name] === wrapper)

        val sObjects = container.getObjectsOfType(sourceTypeRef)
        assert(sObjects.isNotEmpty())
        assert(sObjects[name] === wrapper)
    }
}