package io.github.shoaky.sourcedownloader.common

import io.github.shoaky.sourcedownloader.sdk.MapPatternVariables
import io.github.shoaky.sourcedownloader.sdk.PatternVariables
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.VariableProvider
import io.github.shoaky.sourcedownloader.sdk.util.TextClear
import org.apache.commons.lang3.math.NumberUtils
import org.slf4j.LoggerFactory
import kotlin.io.path.nameWithoutExtension

/**
 * 从文件名中提取出集数
 */
object EpisodeVariableProvider : VariableProvider {

    private val log = LoggerFactory.getLogger(EpisodeVariableProvider::class.java)

    private val parserChain: List<ValueParser> = listOf(
        RegexValueParser(Regex("第(\\d+(?:\\.\\d)?)[话話集巻]")),
        RegexValueParser(Regex("(\\d+(?:\\.\\d)?)[話话]")),
        RegexValueParser("EP(\\d+)".toRegex(RegexOption.IGNORE_CASE)),
        RegexValueParser("S(\\d+)E(\\d+)".toRegex(RegexOption.IGNORE_CASE)),
        RegexValueParser("E(\\d+)".toRegex()),
        RegexValueParser("Episode (\\d+)".toRegex()),
        WordEpisodeValueParser,
        // 连续数字只出现过一次的
        RegexValueParser("^\\D*?(\\d+)\\D*?\$".toRegex()),
        RegexValueParser("#(\\d+)".toRegex()),
        CommonEpisodeValueParser,
    )

    private val textClear = TextClear(
        mapOf(
            Regex("(?:480|720|1080|2160)P", RegexOption.IGNORE_CASE) to "",
            Regex("(1920x1080|3840x2160)", RegexOption.IGNORE_CASE) to "",
            Regex("x(?:264|265)", RegexOption.IGNORE_CASE) to "",
            Regex("flacx2|ma10p|hi10p|yuv420p10|10bit|hevc10|aacx2|flac|4k|_", RegexOption.IGNORE_CASE) to "",
            Regex("\\b[A-Fa-f0-9]{8}\\b|\\w+-\\d+|(\\d){5,}", RegexOption.IGNORE_CASE) to "",
            Regex("v\\d+") to "",
            Regex("FIN", RegexOption.IGNORE_CASE) to ""
        )
    )

    override fun itemSharedVariables(sourceItem: SourceItem): PatternVariables = PatternVariables.EMPTY

    override fun itemFileVariables(
        sourceItem: SourceItem,
        sharedVariables: PatternVariables,
        sourceFiles: List<SourceFile>,
    ): List<PatternVariables> {
        return sourceFiles.map { file ->
            val string = textClear.input(file.path.nameWithoutExtension)
            val episode = parserChain.firstNotNullOfOrNull {
                log.debug("Parser {}", it)
                it.parse(string)
            }

            val vars = MapPatternVariables()
            padNumber(episode)?.run {
                vars.addVariable("episode", this)
            }
            vars
        }
    }

    override fun support(sourceItem: SourceItem): Boolean = true

    override val accuracy: Int = 3

    private fun padNumber(number: Number?, length: Int = 2): String? {
        val str = number?.toString() ?: return null
        if (str.contains(".")) {
            val index = str.indexOf(".")
            val substring = str.substring(0, index)
            val padded = substring.padStart(length, '0')
            return padded + str.substring(index)
        }
        return str.padStart(length, '0')
    }

    override fun extractFrom(text: String): String {
        val episode = parserChain.firstNotNullOfOrNull {
            it.parse(text)
        }
        return padNumber(episode) ?: text
    }
}

private interface ValueParser {

    fun parse(value: String): Number?
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

private object CommonEpisodeValueParser : ValueParser {

    private val pattern = Regex("(\\[?[^\\[\\]]*]?)")

    private val replaces = mapOf(
        Regex("[！？]") to " ",
        Regex("【") to "[",
        Regex("】") to "]",
        Regex("\\(") to "[",
        Regex("\\)") to "]",
        Regex("(?<=\\d)集") to "",
        // 匹配[12 xxx]提取[12]
        Regex("\\[(\\d+)\\s(.*?)]") to "[$1]",
        // 匹配[any]但是除了纯数字的内容
        Regex("\\[(?!\\d+])[^\\[\\]]*?(.*?)]") to "",
    )

    override fun parse(value: String): Number? {
        val replace = pattern.replace(replace(value)) { "${it.value.trim()} " }
        val episode = replace.split(" ")
            .filter { it.isNotBlank() }
            .map { it.removePrefix("[").removeSuffix("]") }
            .lastOrNull { NumberUtils.isParsable(it) && it.length > 1 }
            ?.let {
                if (it.contains(".")) {
                    it.toFloat()
                } else {
                    it.toInt()
                }
            }
        return episode
    }

    private fun replace(filename: String): String {
        var res = filename
        replaces.forEach { (key, value) ->
            res = res.replace(key, value)
        }
        return res
    }
}

private object WordEpisodeValueParser : ValueParser {

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