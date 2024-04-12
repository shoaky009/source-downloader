package io.github.shoaky.sourcedownloader.common

import io.github.shoaky.sourcedownloader.external.season.*
import io.github.shoaky.sourcedownloader.sdk.PatternVariables
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.VariableProvider

/**
 * 从文件名或title中提取季度
 */
object SeasonVariableProvider : VariableProvider {

    private val seasonSupport = SeasonSupport(
        listOf(
            SpSeasonParser,
            GeneralSeasonParser,
            LastStringSeasonParser,
            ContainsSeasonKeyword,
            ExtractTitleSeasonParser
        ),
        true
    )

    override fun itemVariables(sourceItem: SourceItem): PatternVariables = PatternVariables.EMPTY

    override fun fileVariables(
        sourceItem: SourceItem,
        itemVariables: PatternVariables,
        sourceFiles: List<SourceFile>,
    ): List<PatternVariables> {
        // 顺序filename, parent, title
        return sourceFiles.map { file ->
            val filepath = file.path.toString()
            val seasonNumber = seasonSupport.input(
                ParseValue(filepath),
                ParseValue(sourceItem.title),
            )
            val season = seasonNumber.toString().padStart(2, '0')
            Season(season)
        }
    }

    override val accuracy: Int = 2

}

private class Season(
    val season: String
) : PatternVariables