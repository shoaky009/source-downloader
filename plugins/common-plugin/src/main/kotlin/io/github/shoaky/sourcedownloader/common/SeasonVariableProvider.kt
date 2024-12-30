package io.github.shoaky.sourcedownloader.common

import io.github.shoaky.sourcedownloader.external.season.*
import io.github.shoaky.sourcedownloader.external.tmdb.TmdbClient
import io.github.shoaky.sourcedownloader.sdk.PatternVariables
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.VariableProvider

/**
 * 从文件名或title中提取季度
 */
object SeasonVariableProvider : VariableProvider {

    private val normalChain = SeasonSupport(
        listOf(
            SpSeasonParser,
            GeneralSeasonParser,
            LastStringSeasonParser,
            ContainsSeasonKeyword,
            ExtractTitleSeasonParser
        ),
        true
    )

    private val extractChain = SeasonSupport(
        listOf(
            SpSeasonParser,
            GeneralSeasonParser,
            LastStringSeasonParser,
            ContainsSeasonKeyword,
            ExtractTitleSeasonParser,
            TmdbSeasonParser(TmdbClient.default, true)
        ),
        true
    )

    override fun fileVariables(
        sourceItem: SourceItem,
        itemVariables: PatternVariables,
        sourceFiles: List<SourceFile>,
    ): List<PatternVariables> {
        // 顺序filename, parent, title
        return sourceFiles.map { file ->
            val filepath = file.path.toString()
            val seasonNumber = normalChain.input(
                ParseValue(filepath),
                ParseValue(sourceItem.title),
            )
            val season = seasonNumber.toString().padStart(2, '0')
            Season(season)
        }
    }

    override fun extractFrom(sourceItem: SourceItem, text: String): PatternVariables? {
        val season = extractChain.input(
            ParseValue(text, preprocessValue = false)
        )?.toString()?.padStart(2, '0')
        return season?.let { Season(it) }
    }

    override fun primary(): String {
        return "season"
    }

    override fun itemVariables(sourceItem: SourceItem): PatternVariables = PatternVariables.EMPTY

    override val accuracy: Int = 2

}

private class Season(
    val season: String
) : PatternVariables