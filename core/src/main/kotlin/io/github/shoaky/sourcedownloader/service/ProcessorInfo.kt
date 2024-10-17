package io.github.shoaky.sourcedownloader.service

import java.time.Instant

data class ProcessorInfo(
    val name: String,
    val enabled: Boolean,
    val category: String?,
    val tags: Set<String>,
    val lastTriggerTime: Instant?,
)