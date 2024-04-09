package io.github.shoaky.sourcedownloader.core.processor

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import io.github.shoaky.sourcedownloader.core.ProcessingContent
import io.github.shoaky.sourcedownloader.core.ProcessingStorage
import io.github.shoaky.sourcedownloader.core.file.CoreItemContent
import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.ProcessContext
import io.github.shoaky.sourcedownloader.sdk.ProcessorInfo
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import java.nio.file.Path

class CoreProcessContext(
    private val processName: String,
    private val processingStorage: ProcessingStorage,
    private val processor: ProcessorInfo,
) : ProcessContext {

    val stat: ProcessStat = ProcessStat(processName)
    private val currentProcessItemPathsMapping: Multimap<CoreItemContent, Path> = ArrayListMultimap.create()
    private val processedItems: MutableList<SourceItem> = mutableListOf()
    private var hasError: Boolean = false
    override fun processor(): ProcessorInfo {
        return processor
    }

    override fun processedItems(): List<SourceItem> {
        return processedItems
    }

    override fun getItemContent(sourceItem: SourceItem): ItemContent {
        return processingStorage.findByNameAndHash(processName, sourceItem.hashing())?.itemContent
            ?: throw IllegalStateException("Item content not found: $sourceItem")
    }

    override fun hasError(): Boolean {
        return hasError
    }

    @Synchronized
    fun touch(content: ProcessingContent) {
        processedItems.add(content.itemContent.sourceItem)
        if (hasError.not() && content.status == ProcessingContent.Status.FAILURE) {
            hasError = true
        }
    }

    @Synchronized
    fun addItemPaths(sourceItem: CoreItemContent, paths: Collection<Path>) {
        val p = currentProcessItemPathsMapping[sourceItem]
        p.addAll(paths)
    }

    @Synchronized
    fun removeItemPaths(sourceItem: SourceItem) {
        currentProcessItemPathsMapping.removeAll(sourceItem)
    }

    @Synchronized
    fun findItems(path: Path): List<CoreItemContent> {
        return currentProcessItemPathsMapping.entries()
            .filter { it.value == path }
            .map { it.key }
    }

}