package xyz.shoaky.sourcedownloader.sdk

import xyz.shoaky.sourcedownloader.sdk.component.ComponentType

data class PatternVarsDescription(
    val componentType: ComponentType,
    val vars: List<VarDescription>
)

data class VarDescription(
    val name: String,
    val nonNull: Boolean = true,
    val description: String? = null
)