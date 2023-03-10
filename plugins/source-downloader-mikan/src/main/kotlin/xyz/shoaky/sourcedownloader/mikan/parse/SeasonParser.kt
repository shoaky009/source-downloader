package xyz.shoaky.sourcedownloader.mikan.parse

import java.util.function.Function
import java.util.regex.Pattern

internal object SeasonParser : ValueParser {

    override val name: String = "SeasonFromSubject"

    override fun apply(subjectContent: SubjectContent, filename: String): Result {
        val subjectName = subjectContent.nonEmptyName().trim()
        for (rule in rules) {
            val res = rule.ifMatchConvert(subjectName)
                ?: rule.ifMatchConvert(filename)
            if (res != null) {
                return Result(res, accuracy = Result.Accuracy.ACCURATE)
            }
        }

        val season = general.ifMatchConvert(filename)
        if (season != null) {
            return Result(season, accuracy = Result.Accuracy.ACCURATE)
        }
        return Result()
    }

    internal data class SeasonRule(private val pattern: Pattern, val convert: Function<String, Int>) {
        fun ifMatchConvert(name: String): Int? {
            val matcher = pattern.matcher(name)
            if (matcher.find()) {
                return convert.apply(matcher.group())
            }
            return null
        }
    }

    private val lastMatchPattern = Pattern.compile("(?:\\d+|二|三|四|五|六|七|八|九|十|II|III|IV|V|VI|VII|VIII|IX|X|Ⅱ|Ⅲ|Ⅳ)\$")

    //标题最后是数字的
    private val last =
        SeasonRule(lastMatchPattern) {
            it.toIntOrNull() ?: seasonNumberMapping[it]
            ?: throw RuntimeException("can't parse season number from $it")
        }

    //常见的比如第X季 第X期 S Season
    private val general = SeasonRule(Pattern.compile("S\\d{1,2}|Season \\d{1,2}|第.[季期]|\\d+(?i)nd")) {
        val s = it.replace("S", "", true)
            .replace("Season", "", true)
            .replace("第", "")
            .replace("季", "")
            .replace("期", "")
            .replace("nd", "", true)
            .trim()
        seasonNumberMapping[s] ?: s.toInt()
    }

    //99%是季度命名的规则
    private val rules = listOf(
        general,
        last,
    )

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

}

