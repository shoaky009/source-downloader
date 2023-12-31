package io.github.shoaky.sourcedownloader.common

import io.github.shoaky.sourcedownloader.external.season.*
import io.github.shoaky.sourcedownloader.sdk.*
import io.github.shoaky.sourcedownloader.sdk.component.VariableProvider

/**
 * 从文件名或title中提取季度
 */
object SeasonVariableProvider : VariableProvider {

    private val seasonSupport = SeasonSupport(
        listOf(
            SpSeasonParser,
            GeneralSeasonParser,
            LastStringSeasonParser
        ),
        true
    )

    // 顺序filename, parent, title
    override fun createItemGroup(sourceItem: SourceItem): SourceItemGroup {
        return FunctionalItemGroup { file ->
            val seasonNumber = seasonSupport.input(
                ParseValue(file.path.toString()),
                ParseValue(sourceItem.title),
            )
            val season = seasonNumber.toString().padStart(2, '0')
            UniversalFileVariable(Season(season))
        }
    }

    override fun support(sourceItem: SourceItem): Boolean = true
    override val accuracy: Int = 2

}

private class Season(
    val season: String
) : PatternVariables