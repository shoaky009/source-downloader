package io.github.shoaky.sourcedownloader.service

import io.github.shoaky.sourcedownloader.sdk.component.ComponentRootType

data class ComponentCreateBody(
    val type: ComponentRootType,
    val typeName: String,
    val name: String,
    val props: Map<String, Any>,
)