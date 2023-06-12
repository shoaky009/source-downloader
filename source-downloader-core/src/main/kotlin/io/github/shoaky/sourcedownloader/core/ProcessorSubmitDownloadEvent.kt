package io.github.shoaky.sourcedownloader.core

import io.github.shoaky.sourcedownloader.sdk.SourceContent

data class ProcessorSubmitDownloadEvent(
    val processorName: String,
    val sourceContent: SourceContent
)