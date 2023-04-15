package xyz.shoaky.sourcedownloader.core

import xyz.shoaky.sourcedownloader.sdk.SourceContent

class ProcessorSubmitDownloadEvent(
    val processorName: String,
    val sourceContent: SourceContent
)