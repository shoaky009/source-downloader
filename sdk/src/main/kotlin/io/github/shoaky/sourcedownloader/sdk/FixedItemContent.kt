package io.github.shoaky.sourcedownloader.sdk

data class FixedItemContent(
    override val sourceItem: SourceItem,
    override val sourceFiles: List<FileContent>,
    override val sharedPatternVariables: PatternVariables = PatternVariables.EMPTY
) : ItemContent {

    override fun summaryContent(): String = ""
}