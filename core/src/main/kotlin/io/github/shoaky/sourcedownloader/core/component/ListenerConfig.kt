package io.github.shoaky.sourcedownloader.core.component

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.github.shoaky.sourcedownloader.core.processor.ListenerMode

@JsonDeserialize(using = ListenerConfigDeserializer::class)
data class ListenerConfig(
    val id: ComponentId,
    val mode: ListenerMode = ListenerMode.EACH
)

