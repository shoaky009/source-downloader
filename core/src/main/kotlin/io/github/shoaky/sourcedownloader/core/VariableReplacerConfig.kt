package io.github.shoaky.sourcedownloader.core

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.github.shoaky.sourcedownloader.core.component.ComponentId

@JsonDeserialize(using = VariableReplacerConfigDeserializer::class)
data class VariableReplacerConfig(
    val id: ComponentId,
    val keys: Set<String>? = null,
)