package io.github.shoaky.sourcedownloader.common

import io.github.shoaky.sourcedownloader.external.season.GeneralSeasonParser
import io.github.shoaky.sourcedownloader.external.season.ParseValue
import io.github.shoaky.sourcedownloader.external.season.SeasonSupport
import io.github.shoaky.sourcedownloader.external.season.SpSeasonParser
import io.github.shoaky.sourcedownloader.sdk.*
import io.github.shoaky.sourcedownloader.sdk.component.VariableProvider

/**
 * 从文件名或title中提取季度
 */
// TODO 改成title, filename, parent有不同的规则
object SeasonVariableProvider : VariableProvider {

    private val seasonSupport = SeasonSupport(
        listOf(
            SpSeasonParser,
            GeneralSeasonParser,
        ),
        true
    )

    // 顺序filename, parent, title
    override fun createSourceGroup(sourceItem: SourceItem): SourceItemGroup {
        return FunctionalItemGroup { file ->
            val seasonNumber = seasonSupport.input(
                ParseValue(file.path.toString()),
                ParseValue(sourceItem.title),
            )
            val season = seasonNumber.toString().padStart(2, '0')
            UniversalFileVariable(Season(season))
        }
    }

    override fun support(item: SourceItem): Boolean = true
    override val accuracy: Int = 2

}

private class Season(
    val season: String
) : PatternVariables