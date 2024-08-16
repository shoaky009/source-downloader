package io.github.shoaky.sourcedownloader.common.anime

import com.google.common.base.CharMatcher
import io.github.shoaky.sourcedownloader.sdk.PatternVariables
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.VariableProvider
import io.github.shoaky.sourcedownloader.sdk.util.TextClear

object AnimeTitleVariableProvider : VariableProvider {

    private val chain: List<Extractor> = listOf(
        AniTitleExtractor
    )

    private val clear = TextClear(
        mapOf(Regex("（僅限港澳台）") to "")
    )

    override fun itemVariables(sourceItem: SourceItem): PatternVariables {
        val title = clear.input(sourceItem.title)
        for (extractor in chain) {
            val result = extractor.extract(title)
            if (result != null) {
                return result
            }
        }
        return PatternVariables.EMPTY
    }

    override fun primary(): String {
        return "title"
    }

}

interface Extractor {

    fun extract(raw: String): Titles?
}

data class Titles(
    val title: String,
    val romaji: String? = null,
) : PatternVariables

/**
 * 从[ANI]到集数之间的内容，再判断是否需要提取罗马或标题
 */
object AniTitleExtractor : Extractor {

    private const val GROUP = "[ANi]"
    private val EPISODE_REGEX = Regex(" - \\d+(\\.\\d+)? \\[.*]")
    private const val INVALID_INDEX = -1
    private const val SPECIAL_CHAR = " - "
    override fun extract(raw: String): Titles? {
        val groupIndex = raw.indexOf("[ANI]", ignoreCase = true)
        if (groupIndex == INVALID_INDEX) {
            return null
        }
        val startIndex = groupIndex + GROUP.length
        val endIndex = EPISODE_REGEX.find(raw)?.range?.start ?: -1
        if (endIndex == INVALID_INDEX) {
            return null
        }
        val title = raw.substring(startIndex, endIndex)
        val multiTitle = title.split(SPECIAL_CHAR)
        if (multiTitle.isEmpty()) {
            return null
        }
        if (multiTitle.size == 1) {
            return Titles(multiTitle.first().trim())
        }
        if (multiTitle.size == 2) {
            val (title1, title2) = multiTitle
            val allOf = CharMatcher.ascii().matchesAllOf(title1)
            if (allOf) {
                return Titles(title2.trim(), title1.trim())
            }
            return Titles(title1.trim(), title2.trim())
        }
        // 偷懒先看效果
        return Titles(multiTitle.last().trim())
    }

}