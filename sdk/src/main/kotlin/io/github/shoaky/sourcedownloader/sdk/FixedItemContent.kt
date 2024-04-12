package io.github.shoaky.sourcedownloader.sdk

data class FixedItemContent(
    override val sourceItem: SourceItem,
    override val fileContents: List<FileContent>,
    override val itemVariables: PatternVariables = PatternVariables.EMPTY
) : ItemContent {

    override fun summaryContent(): String = ""
}