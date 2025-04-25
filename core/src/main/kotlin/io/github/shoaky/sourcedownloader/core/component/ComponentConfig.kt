package io.github.shoaky.sourcedownloader.core.component

import com.fasterxml.jackson.annotation.JsonInclude
import io.github.shoaky.sourcedownloader.sdk.component.ComponentRootType
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
data class ComponentConfig(
    val name: String,
    val type: String,
    val props: Map<String, Any> = emptyMap(),
) {

    fun instanceName(topType: ComponentRootType): String {
        return ComponentType.of(topType, type).instanceName(name)
    }
}