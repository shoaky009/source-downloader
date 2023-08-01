package io.github.shoaky.sourcedownloader.common

import io.github.shoaky.sourcedownloader.sdk.*
import io.github.shoaky.sourcedownloader.sdk.component.VariableProvider
import io.github.shoaky.sourcedownloader.sdk.util.replaces
import kotlin.io.path.nameWithoutExtension

// 先写个最简单的
object LanguageVariableProvider : VariableProvider {

    private val replaces = listOf("-", "_", "[", "]", "(", ")", ".")
    private val languages = mapOf(
        " chs".toRegex(RegexOption.IGNORE_CASE) to "zh-CHS",
        " cht".toRegex(RegexOption.IGNORE_CASE) to "zh-CHT",
        " sc".toRegex(RegexOption.IGNORE_CASE) to "zh-CHS",
        " tc".toRegex(RegexOption.IGNORE_CASE) to "zh-CHT",
    )

    override fun createSourceGroup(sourceItem: SourceItem): SourceItemGroup {
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

    override fun support(item: SourceItem): Boolean = true

}