package io.github.shoaky.sourcedownloader.component.source

import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.AlwaysLatestSource
import io.github.shoaky.sourcedownloader.sdk.component.ItemFileResolver

class FixedSource(
    private val content: List<SourceItemContent>
) : AlwaysLatestSource(), ItemFileResolver {

    override fun fetch(): Iterable<SourceItem> {
        return content.map { it.item }
    }

    override fun resolveFiles(sourceItem: SourceItem): List<SourceFile> {
        return content.find { it.item == sourceItem }?.files ?: emptyList()
    }
}

data class SourceItemContent(
    val item: SourceItem,
    val files: List<SourceFile>
)