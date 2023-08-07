package io.github.shoaky.sourcedownloader.sdk

data class FixedItemContent(
    override val sourceItem: SourceItem,
    override val sourceFiles: List<FileContent>
) : ItemContent