package xyz.shoaky.sourcedownloader.component.provider

import org.apache.commons.lang3.math.NumberUtils
import xyz.shoaky.sourcedownloader.sdk.*
import xyz.shoaky.sourcedownloader.sdk.component.VariableProvider
import java.nio.file.Path
import java.util.function.Function
import java.util.regex.Pattern
import kotlin.io.path.nameWithoutExtension

object SeasonProvider : VariableProvider {

    private val list: List<Function<Pair<SourceItem, Path>, Int?>> = listOf(
        GeneralSeason,
    )

    // 后面改成调用链
    override fun createSourceGroup(sourceItem: SourceItem): SourceItemGroup {
        return FunctionalItemGroup { path ->
            var seasonNumber = 1
            for (function in list) {
                val apply = function.apply(sourceItem to path)
                if (apply != null) {
                    seasonNumber = apply
                    break
                }
            }
            val season = seasonNumber.toString().padStart(2, '0')
            UniversalSourceFile(Season(season))
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
            val res = rule.ifMatchConvert(sourceItem.title)
                ?: rule.ifMatchConvert(path.nameWithoutExtension)
            if (res != null) {
                return res
            }
        }
        return null
    }

    private val lastMatchPattern = Pattern.compile("(?:\\d+|二|三|四|五|六|七|八|九|十|II|III|IV|V|VI|VII|VIII|IX|X|Ⅱ|Ⅲ|Ⅳ)\$")

    // 常见的比如第X季 第X期 S Season
    private val general = RegexRule(Pattern.compile("S\\d{1,2}|Season \\d{1,2}|第.[季期]|\\d+(?i:rd|nd)")) {
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

    // 标题最后是数字的
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
        general,
        last,
    )

    private data class RegexRule(private val pattern: Pattern, val convert: Function<String, Int?>) {
        fun ifMatchConvert(name: String): Int? {
            val matcher = pattern.matcher(name)
            if (matcher.find()) {
                return convert.apply(matcher.group())
            }
            return null
        }
    }
}

private class Season(
    val season: String
) : PatternVariables