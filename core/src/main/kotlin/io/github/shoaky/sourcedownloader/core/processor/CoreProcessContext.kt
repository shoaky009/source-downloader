package io.github.shoaky.sourcedownloader.core.processor

import io.github.shoaky.sourcedownloader.core.ProcessingContent
import io.github.shoaky.sourcedownloader.core.ProcessingStorage
import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.ProcessContext
import io.github.shoaky.sourcedownloader.sdk.ProcessorInfo
import io.github.shoaky.sourcedownloader.sdk.SourceItem

class CoreProcessContext(
    private val processName: String,
    private val processingStorage: ProcessingStorage,
    override val processor: ProcessorInfo,
) : ProcessContext {

    private val sourceItems: MutableList<SourceItem> = mutableListOf()
    private var hasError: Boolean = false

    override fun processedItems(): List<SourceItem> {
        return sourceItems
    }

    override fun getItemContent(sourceItem: SourceItem): ItemContent {
        return processingStorage.findByNameAndHash(processName, sourceItem.hashing())?.itemContent
            ?: throw IllegalStateException("Item content not found: $sourceItem")
    }

    override fun hasError(): Boolean {
        return hasError
    }

    fun touch(content: ProcessingContent) {
        sourceItems.add(content.itemContent.sourceItem)
        if (hasError.not() && content.status == ProcessingContent.Status.FAILURE) {
            hasError = true
        }
    }

}