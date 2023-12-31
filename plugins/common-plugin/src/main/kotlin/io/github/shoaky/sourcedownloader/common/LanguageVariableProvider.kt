package io.github.shoaky.sourcedownloader.common

import io.github.shoaky.sourcedownloader.sdk.*
import io.github.shoaky.sourcedownloader.sdk.component.VariableProvider
import io.github.shoaky.sourcedownloader.sdk.util.replaces
import kotlin.io.path.nameWithoutExtension

/**
 * 简单地通过文件名中的语言标识符来提供语言变量
 * chs,sc -> zh-CHS
 * cht,tc -> zh-CHT
 * ...
 */
object LanguageVariableProvider : VariableProvider {

    private val replaces = listOf("-", "_", "[", "]", "(", ")", ".")
    private val languages = mapOf(
        " chs| sc| gb".toRegex(RegexOption.IGNORE_CASE) to "zh-CHS",
        " cht| tc| big5".toRegex(RegexOption.IGNORE_CASE) to "zh-CHT",
        "jpsc".toRegex(RegexOption.IGNORE_CASE) to "ja-JP.zh-CHS",
        "jptc".toRegex(RegexOption.IGNORE_CASE) to "ja-JP.zh-CHT",
    )

    override fun createItemGroup(sourceItem: SourceItem): SourceItemGroup {
        return FunctionalItemGroup(
            function = {
                val name = it.path.nameWithoutExtension.replaces(replaces, " ")
                val language = languages.entries.firstOrNull { (regex, _) -> regex.containsMatchIn(name) }?.value
                    ?: return@FunctionalItemGroup FileVariable.EMPTY

                val variables = MapPatternVariables(mapOf("language" to language))
                UniversalFileVariable(variables)
            }
        )
    }

    override fun support(sourceItem: SourceItem): Boolean = true

}