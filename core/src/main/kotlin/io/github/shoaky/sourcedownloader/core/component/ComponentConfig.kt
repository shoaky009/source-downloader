package io.github.shoaky.sourcedownloader.core.component

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
data class ComponentConfig(
    val name: String,
    val type: String,
    val props: Map<String, Any> = emptyMap(),
)