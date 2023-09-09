package io.github.shoaky.sourcedownloader.external.season

import org.apache.commons.lang3.CharUtils
import org.apache.commons.lang3.math.NumberUtils
import java.util.function.Function

object GeneralSeasonParser : SeasonParser {

    override fun input(subject: String): SeasonResult? {
        generalSeasonRegex.ifMatchConvert(subject)?.let {
            return SeasonResult(it, SeasonResult.Accuracy.ACCURATE)
        }
        return null
    }

    // 常见的比如第X季 第X期 S Season
    private val generalSeasonRegex =
        RegexRule("S\\d{1,2}|Season \\d{1,2}|第.[季期]|\\d+(?i:rd|nd)".toRegex(RegexOption.IGNORE_CASE)) {
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

    val seasonNumberMapping: Map<String, Int> = mapOf(
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

}

data class RegexRule(private val regex: Regex, val convert: Function<String, Int?>) {

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