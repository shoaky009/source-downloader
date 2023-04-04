package xyz.shoaky.sourcedownloader.core.idk

import xyz.shoaky.sourcedownloader.sdk.SourceContent
import xyz.shoaky.sourcedownloader.sdk.SourceItem

data class PersistentSourceContent(
    override val sourceItem: SourceItem,
    override val sourceFiles: List<PersistentFileContent>,
    val options: Options = Options(),
) : SourceContent {

    data class Options(
        val parsingFailsUsingTheOriginal: Boolean = false,
        // val parsingFailedStrategy: ParsingFailedStrategy = ParsingFailedStrategy.USE_ORIGINAL_FILENAME
    )
}

data class IdkSourceContent(
    private val persistentSourceContent: PersistentSourceContent,
) : SourceContent by persistentSourceContent