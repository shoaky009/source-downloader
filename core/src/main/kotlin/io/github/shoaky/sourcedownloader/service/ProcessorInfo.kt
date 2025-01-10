package io.github.shoaky.sourcedownloader.service

import io.github.shoaky.sourcedownloader.core.processor.ProcessorRuntime

data class ProcessorInfo(
    val name: String,
    val enabled: Boolean,
    val category: String?,
    val tags: Set<String>,
    val runtime: ProcessorRuntime.Snapshot?,
)