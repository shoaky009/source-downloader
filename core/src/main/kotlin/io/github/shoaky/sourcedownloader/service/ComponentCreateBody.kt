package io.github.shoaky.sourcedownloader.service

import io.github.shoaky.sourcedownloader.sdk.component.ComponentTopType

data class ComponentCreateBody(
    val type: ComponentTopType,
    val typeName: String,
    val name: String,
    val props: Map<String, Any>,
)