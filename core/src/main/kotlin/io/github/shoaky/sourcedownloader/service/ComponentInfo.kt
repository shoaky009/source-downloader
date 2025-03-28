package io.github.shoaky.sourcedownloader.service

import io.github.shoaky.sourcedownloader.sdk.component.ComponentTopType

data class ComponentInfo(
    val type: ComponentTopType,
    val typeName: String,
    val name: String,
    val props: Map<String, Any>,
    val stateDetail: Any? = null,
    val primary: Boolean,
    val running: Boolean,
    val refs: Set<String>?,
    val modifiable: Boolean = true,
    val errorMessage: String? = null
)