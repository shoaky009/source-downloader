package io.github.shoaky.sourcedownloader.common

import io.github.shoaky.sourcedownloader.sdk.MapPatternVariables
import io.github.shoaky.sourcedownloader.sdk.PatternVariables
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.VariableProvider
import io.github.shoaky.sourcedownloader.sdk.util.TextClear
import org.apache.commons.lang3.math.NumberUtils
import org.slf4j.LoggerFactory
import java.io.Serializable
import kotlin.io.path.nameWithoutExtension

/**
 * 从文件名中提取出集数
 */
object EpisodeVariableProvider : VariableProvider {

    private val log = LoggerFactory.getLogger(EpisodeVariableProvider::class.java)

    private val parserChain: List<ValueParser> = listOf(
        RegexValueParser(Regex("第(\\d+(?:\\.\\d)?)[话話集巻]")),
        RegexValueParser(Regex("(\\d+(?:\\.\\d)?)[話话]")),
        RegexValueParser("(E|EP|Episode |Episode)(\\d+)(?![\\d-])".toRegex(RegexOption.IGNORE_CASE)),
        RegexValueParser("S(\\d+)E(\\d+)".toRegex(RegexOption.IGNORE_CASE)),
        RegexValueParser("SP(\\d+)".toRegex()),
        WordEpisodeValueParser,
        // 连续数字只出现过一次的
        RegexValueParser("^\\D*?(\\d{1,3})\\D*?\$".toRegex()),
        RegexValueParser("#(\\d+)".toRegex()),
        RangeEpisodeValueParser,
        CommonEpisodeValueParser,
        //[01(56)]
        RegexValueParser("\\[(\\d{2})\\(\\d{2}\\)]".toRegex()),
    )

    private val textClear = TextClear(
        mapOf(
            Regex("_") to " ",
            Regex("(?:480|720|1080|2160)P", RegexOption.IGNORE_CASE) to "",
            Regex("(1920x1080|3840x2160)", RegexOption.IGNORE_CASE) to "",
            Regex("x(?:264|265)", RegexOption.IGNORE_CASE) to "",
            Regex("flacx2|ma10p|hi10p|yuv420p10|10bit|hevc10|aacx2|flac|4k", RegexOption.IGNORE_CASE) to "",
            Regex("\\b[A-Fa-f0-9]{8}\\b|CRC32.*[0-9A-F]{8}", RegexOption.IGNORE_CASE) to "",
            Regex("v\\d+|(\\d){5,}") to "",
            Regex("FIN", RegexOption.IGNORE_CASE) to "",
            Regex("1st|2nd|3rd|[4-9]th", RegexOption.IGNORE_CASE) to ""
        )
    )

    override fun itemVariables(sourceItem: SourceItem): PatternVariables = PatternVariables.EMPTY

    override fun fileVariables(
        sourceItem: SourceItem,
        itemVariables: PatternVariables,
        sourceFiles: List<SourceFile>,
    ): List<PatternVariables> {
        return sourceFiles.map { file ->
            val string = textClear.input(file.path.nameWithoutExtension)
            val episode = parserChain.firstNotNullOfOrNull {
                val value = it.parse(string)
                if (value != null) {
                    log.debug("Parser:{} extract:{} episode:{}", it, string, value)
                }
                value
            }

            val vars = MapPatternVariables()
            padNumber(episode)?.run {
                vars.addVariable("episode", this)
            }
            vars
        }
    }

    override val accuracy: Int = 3

    private fun padNumber(number: Serializable?, length: Int = 2): String? {
        val str = number?.toString() ?: return null
        if (str.contains(".")) {
            val index = str.indexOf(".")
            val substring = str.substring(0, index)
            val padded = substring.padStart(length, '0')
            return padded + str.substring(index)
        }
        return str.padStart(length, '0')
    }

    override fun extractFrom(text: String): PatternVariables? {
        return parserChain.firstNotNullOfOrNull {
            it.parse(text)
        }?.let {
            MapPatternVariables(mapOf("episode" to padNumber(it).toString()))
        }
    }

    override fun primary(): String {
        return "episode"
    }
}

private interface ValueParser {

    fun parse(value: String): Serializable?
}

private class RegexValueParser(
    private val regex: Regex
) : ValueParser {

    override fun parse(value: String): Number? {
        val string = regex.find(value)?.groupValues?.lastOrNull() ?: return null
        if (string.contains(".")) {
            return string.toFloat()
        }
        return string.toInt()
    }

    override fun toString(): String {
        return "RegexValueParser(regex=$regex)"
    }
}

private data object CommonEpisodeValueParser : ValueParser {

    private val pattern = Regex("(\\[?[^\\[\\]]*]?)")

    private val replaces = mapOf(
        Regex("[！？]") to " ",
        Regex("[【(]") to "[",
        Regex("[】)]") to "]",
        Regex("(?<=\\d)集") to "",
        // 匹配[12 xxx]提取[12]
        Regex("\\[(\\d+)\\s(.*?)]") to "[$1]",
        // 匹配[any]但是除了纯数字的内容
        Regex("\\[(?!\\d+])[^\\[\\]]*?(.*?)]") to "",
    )

    private val resolutionNumber = setOf(480, 720, 1080, 2160)

    override fun parse(value: String): Number? {
        val replace = pattern.replace(replace(value)) { "${it.value.trim()} " }
        val episodes = replace.split(" ")
            .filter { it.isNotBlank() }
            .mapNotNull {
                val raw = it.removePrefix("[").removeSuffix("]")
                if (NumberUtils.isParsable(raw) && raw.length > 1) {
                    return@mapNotNull Candidate(
                        raw,
                        it.startsWith("[") && it.endsWith("]")
                    )
                }
                null
            }
        return episodes.maxOrNull()?.toNumber()
    }

    private fun replace(filename: String): String {
        var res = filename
        replaces.forEach { (key, value) ->
            res = res.replace(key, value)
        }
        return res
    }

    private data class Candidate(
        val numberString: String,
        val bracketSurround: Boolean,
    ) : Comparable<Candidate> {

        fun toNumber(): Number {
            return if (numberString.contains(".")) {
                numberString.toFloat()
            } else {
                numberString.toInt()
            }
        }

        fun getScore(): Int {
            var score = 0
            if (bracketSurround) {
                score += 2
            }
            val number = toNumber()
            if (resolutionNumber.contains(number)) {
                score -= 1
            }
            if (numberString.length == 3) {
                score -= 2
            }
            if (numberString.length == 4) {
                score -= 3
            }
            if (numberString.length > 4) {
                score -= Int.MAX_VALUE
            }
            return score
        }

        override fun compareTo(other: Candidate): Int {
            return this.getScore().compareTo(other.getScore())
        }
    }
}

private data object WordEpisodeValueParser : ValueParser {

    private val wordChain = listOf(
        Regex("第([一二三四五六七八九十])[话話集巻]"),
        Regex("([一二三四五六七八九十])[话話]"),
        Regex("其\\w[壱弐参肆伍陸漆捌玖拾]"),
    )

    private val wordNumberMapping = mapOf(
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
        "壱" to 1,
        "弐" to 2,
        "参" to 3,
        "肆" to 4,
        "伍" to 5,
        "陸" to 6,
        "漆" to 7,
        "捌" to 8,
        "玖" to 9,
        "拾" to 10,
    )

    override fun parse(value: String): Number? {
        val matchedValue = wordChain.firstNotNullOfOrNull { it.find(value)?.groupValues?.lastOrNull() } ?: return null
        return wordNumberMapping[matchedValue]
    }

}

private object RangeEpisodeValueParser : ValueParser {

    private val rangeRegexes = listOf(
        // "(EP|E)(?<begin>\\d+)-(?<end>\\d+)".toRegex(RegexOption.IGNORE_CASE),
        "(?<begin>\\d+)-(?<end>\\d+)".toRegex(RegexOption.IGNORE_CASE),
    )

    override fun parse(value: String): Serializable? {
        for (rangeRegex in rangeRegexes) {
            val matchResult = rangeRegex.find(value) ?: continue
            val begin = matchResult.groups["begin"]?.value ?: continue
            val end = matchResult.groups["end"]?.value ?: continue
            if (begin >= end) {
                return null
            }
            return "$begin-$end"
        }
        return null
    }
}