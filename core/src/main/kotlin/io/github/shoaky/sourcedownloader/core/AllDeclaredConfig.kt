package io.github.shoaky.sourcedownloader.core

import com.fasterxml.jackson.annotation.JsonInclude
import io.github.shoaky.sourcedownloader.core.component.ComponentConfig

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
data class AllDeclaredConfig(
    val components: MutableMap<String, MutableList<ComponentConfig>> = mutableMapOf(),
    val processors: MutableList<ProcessorConfig> = mutableListOf(),
    val instances: MutableList<InstanceConfig> = mutableListOf()
)