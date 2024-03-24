package io.github.shoaky.sourcedownloader.sdk

interface ProcessContext {

    fun processor(): ProcessorInfo

    /**
     * Get the items that have been processed
     */
    fun processedItems(): List<SourceItem>

    /**
     * Get the [ItemContent] of the [sourceItem]
     */
    fun getItemContent(sourceItem: SourceItem): ItemContent

    /**
     * @return true if the process has error
     */
    fun hasError(): Boolean

    companion object {

        val empty: ProcessContext = object : ProcessContext {
            override fun processor(): Nothing = throw UnsupportedOperationException()

            override fun processedItems(): List<SourceItem> = emptyList()

            override fun getItemContent(sourceItem: SourceItem): Nothing = throw UnsupportedOperationException()

            override fun hasError(): Boolean = false
        }
    }
}

class FixedProcessContext(
    private val processorInfo: ProcessorInfo,
    private val processedItems: List<SourceItem> = emptyList(),
    private val itemContentMap: Map<SourceItem, ItemContent> = emptyMap(),
    private val hasError: Boolean = false
) : ProcessContext {

    override fun processor(): ProcessorInfo {
        return processorInfo
    }

    override fun processedItems(): List<SourceItem> {
        return processedItems
    }

    override fun getItemContent(sourceItem: SourceItem): ItemContent {
        return itemContentMap[sourceItem] ?: throw IllegalStateException("Item content not found: $sourceItem")
    }

    override fun hasError(): Boolean {
        return hasError
    }
}