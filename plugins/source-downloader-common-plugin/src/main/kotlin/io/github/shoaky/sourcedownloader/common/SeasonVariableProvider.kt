package io.github.shoaky.sourcedownloader.common

import io.github.shoaky.sourcedownloader.sdk.*
import io.github.shoaky.sourcedownloader.sdk.component.VariableProvider
import org.apache.commons.lang3.CharUtils
import org.apache.commons.lang3.math.NumberUtils
import java.nio.file.Path
import java.util.function.Function
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension

/**
 * 从文件名或title中提取季度
 */
// TODO 改成title, filename, parent有不同的规则
object SeasonVariableProvider : VariableProvider {

    private val rules: List<Function<Pair<SourceItem, Path>, Int?>> = listOf(
        GeneralSeason,
        SpSeason,
    )

    override fun createSourceGroup(sourceItem: SourceItem): SourceItemGroup {
        return FunctionalItemGroup { file ->
            var seasonNumber = 1
            for (function in rules) {
                val apply = function.apply(sourceItem to file.path)
                if (apply != null) {
                    seasonNumber = apply
                    break
                }
            }
            val season = seasonNumber.toString().padStart(2, '0')
            UniversalFileVariable(Season(season))
        }
    }

    override fun support(item: SourceItem): Boolean = true
    override val accuracy: Int = 2

}


private object GeneralSeason : Function<Pair<SourceItem, Path>, Int?> {

    override fun apply(t: Pair<SourceItem, Path>): Int? {
        val path = t.second
        val sourceItem = t.first
        for (rule in rules) {
            var res = rule.ifMatchConvert(sourceItem.title)
                ?: rule.ifMatchConvert(path.nameWithoutExtension)
            if (res == null && path.parent != null) {
                res = rule.ifMatchConvert(path.parent.name)
            }

            if (res != null) {
                return res
            }
        }
        return null
    }

    // TODO 最后一个规则不应该用在filename中，多半会是集数
    private val lastMatchPattern = "(?:\\d+|二|三|四|五|六|七|八|九|十|II|III|IV|V|VI|VII|VIII|IX|X|Ⅱ|Ⅲ|Ⅳ)\$".toRegex()

    // 常见的比如第X季 第X期 S Season
    private val generalSeasonRegex = RegexRule("S\\d{1,2}|Season \\d{1,2}|第.[季期]|\\d+(?i:rd|nd)".toRegex(RegexOption.IGNORE_CASE)) {
        val s = it.replace("S", "", true)
            .replace("Season", "", true)
            .replace("第", "")
            .replace("季", "")
            .replace("期", "")
            .replace("nd", "", true)
            .replace("rd", "", true)
            .trim()
        val parsable = NumberUtils.isParsable(s)
        if (parsable) {
            return@RegexRule s.toInt()
        }
        seasonNumberMapping[s] ?: Regex("\\d+").find(s)?.value?.toInt()
    }

    // 标题最后是连续数字的
    private val last = RegexRule(lastMatchPattern) {
        it.toIntOrNull() ?: seasonNumberMapping[it]
        ?: throw RuntimeException("can't parse season number from $it")
    }

    private val seasonNumberMapping: Map<String, Int> = mapOf(
        "一" to 1,
        "二" to 2,
        "三" to 3,
        "四" to 4,
        "五" to 5,
        "六" to 6,
        "七" to 7,
        "八" to 8,
        "九" to 9,
        "十" to 10,
        "II" to 2,
        "III" to 3,
        "IV" to 4,
        "V" to 5,
        "VI" to 6,
        "VII" to 7,
        "VIII" to 8,
        "IX" to 9,
        "X" to 10,
        "Ⅱ" to 2,
        "Ⅲ" to 3,
        "Ⅳ" to 4,
    )

    // 99%是季度命名的规则
    private val rules = listOf(
        generalSeasonRegex,
        last,
    )

    private data class RegexRule(private val regex: Regex, val convert: Function<String, Int?>) {
        fun ifMatchConvert(target: String): Int? {
            val matcher = regex.find(target)
            if (matcher != null) {
                // 如果前面有字母，那么就不算季度
                if (matcher.range.first > 0) {
                    val prevChar = target[matcher.range.first - 1]
                    if (CharUtils.isAsciiAlpha(prevChar)) {
                        return null
                    }
                }
                return convert.apply(matcher.value)
            }
            return null
        }
    }
}

object SpSeason : Function<Pair<SourceItem, Path>, Int?> {

    private val spRegexes = listOf(
        "OVA|OAD".toRegex(),
        "Special".toRegex(RegexOption.IGNORE_CASE),
        "特别篇|\\[SP]".toRegex(),
        "SPs".toRegex()
    )

    override fun apply(t: Pair<SourceItem, Path>): Int? {
        val sourceItem = t.first
        val path = t.second
        val parentPathName = path.parent?.name
        for (spRegex in spRegexes) {
            var matchResult = spRegex.find(sourceItem.title)
            if (matchResult == null && parentPathName != null) {
                matchResult = spRegex.find(parentPathName)
            }
            if (matchResult == null) {
                matchResult = spRegex.find(path.nameWithoutExtension)
            }
            if (matchResult != null) {
                return 0
            }
        }
        return null
    }

}

private class Season(
    val season: String
) : PatternVariables