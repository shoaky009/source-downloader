package xyz.shoaky.sourcedownloader.core.file

import xyz.shoaky.sourcedownloader.sdk.MapPatternVariables
import xyz.shoaky.sourcedownloader.sdk.SourceContent
import xyz.shoaky.sourcedownloader.sdk.SourceItem

data class PersistentSourceContent(
    override val sourceItem: SourceItem,
    override val sourceFiles: List<CoreFileContent>,
    val sharedPatternVariables: MapPatternVariables
) : SourceContent