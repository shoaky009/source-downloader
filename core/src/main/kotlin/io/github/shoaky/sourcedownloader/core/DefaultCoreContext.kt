package io.github.shoaky.sourcedownloader.core

import com.fasterxml.jackson.core.type.TypeReference
import io.github.shoaky.sourcedownloader.core.component.ComponentId
import io.github.shoaky.sourcedownloader.core.component.ComponentManager
import io.github.shoaky.sourcedownloader.core.component.ComponentWrapper
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.component.ComponentException
import io.github.shoaky.sourcedownloader.sdk.component.ComponentRootType
import io.github.shoaky.sourcedownloader.sdk.component.SdComponent

class DefaultCoreContext(
    private val componentManager: ComponentManager,
    private val type: ComponentRootType,
    private val self: String
) : CoreContext {

    override fun <T : SdComponent> getComponent(
        type: ComponentRootType,
        componentId: String,
        typeReference: TypeReference<T>
    ): T {
        if (type == this.type && componentId == self) {
            throw ComponentException.other("Can't get self component")
        }
        return componentManager.getComponent(
            type,
            ComponentId(componentId),
            object : TypeReference<ComponentWrapper<T>>() {}).get()
    }
}