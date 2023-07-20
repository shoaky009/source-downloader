package io.github.shoaky.sourcedownloader.sdk

data class TestItemContent(
    override val sourceItem: SourceItem,
    override val sourceFiles: List<FileContent>
) : ItemContent