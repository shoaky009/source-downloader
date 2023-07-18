package io.github.shoaky.sourcedownloader.sdk

data class TestSourceContent(
    override val sourceItem: SourceItem,
    override val sourceFiles: List<FileContent>
) : SourceContent