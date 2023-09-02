package io.github.shoaky.sourcedownloader.sdk

interface ProcessContext {

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

}