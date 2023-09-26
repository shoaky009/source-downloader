package io.github.shoaky.sourcedownloader.component.source

import io.github.shoaky.sourcedownloader.sdk.*
import io.github.shoaky.sourcedownloader.sdk.component.ItemFileResolver
import io.github.shoaky.sourcedownloader.sdk.component.Source

/**
 * 返回固定SourceItem和SourceFile
 */
class FixedSource(
    private val content: List<SourceItemContent>,
    private var offsetMode: Boolean = false
) : Source<OffsetPointer>, ItemFileResolver {

    override fun resolveFiles(sourceItem: SourceItem): List<SourceFile> {
        return content.find { it.item == sourceItem }?.files ?: emptyList()
    }

    override fun fetch(pointer: OffsetPointer, limit: Int): Iterable<PointedItem<ItemPointer>> {
        if (offsetMode.not()) {
            return content.map { PointedItem(it.item, NullPointer) }
        }

        return content.map {
            PointedItem(it.item, NullPointer)
        }.drop(pointer.offset).take(limit)
    }

    override fun defaultPointer(): OffsetPointer {
        return OffsetPointer(0)
    }
}

data class SourceItemContent(
    val item: SourceItem,
    val files: List<SourceFile>
)

data class OffsetPointer(
    var offset: Int
) : SourcePointer {

    override fun update(itemPointer: ItemPointer) {
        offset++
    }
}