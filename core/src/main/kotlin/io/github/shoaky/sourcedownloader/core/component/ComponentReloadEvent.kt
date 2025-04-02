package io.github.shoaky.sourcedownloader.core.component

import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

data class ComponentReloadEvent(
    val componentType: ComponentType,
    val componentName: String,
    val refs: Set<String>
) {

    companion object {

        const val ADDRESS = "component:reload"
    }
}
