package io.github.shoaky.sourcedownloader.service

import io.github.shoaky.sourcedownloader.core.ProcessingContent

data class UpdateProcessingContent(
    val status: ProcessingContent.Status? = null,
    val renameTimes: Int? = null
)