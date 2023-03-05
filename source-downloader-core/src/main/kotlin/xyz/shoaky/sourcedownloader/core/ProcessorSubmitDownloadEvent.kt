package xyz.shoaky.sourcedownloader.core

import xyz.shoaky.sourcedownloader.sdk.SourceItem

class ProcessorSubmitDownloadEvent(
    val processorName: String,
    val sourceItem: SourceItem
)