package io.github.shoaky.sourcedownloader.core

import io.github.shoaky.sourcedownloader.sdk.ItemContent

data class ProcessorSubmitDownloadEvent(
    val processorName: String,
    val itemContent: ItemContent
)