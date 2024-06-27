package io.github.shoaky.sourcedownloader.core

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
data class InstanceConfig(
    val name: String,
    val props: Map<String, Any>
)