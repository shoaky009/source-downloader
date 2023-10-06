package io.github.shoaky.sourcedownloader.common

import io.github.shoaky.sourcedownloader.sdk.*
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
        RegexValueParser(Regex("第(\\d+(?:\\.\\d)?)话")),
        RegexValueParser(Regex("第(\\d+(?:\\.\\d)?)集")),
        RegexValueParser(Regex("第(\\d+(?:\\.\\d)?)話")),
        RegexValueParser(Regex("(\\d+(?:\\.\\d)?)話")),
        RegexValueParser(Regex("(\\d+(?:\\.\\d)?)话")),
        RegexValueParser("EP(\\d+)".toRegex(RegexOption.IGNORE_CASE)),
        RegexValueParser("S(\\d+)E(\\d+)".toRegex(RegexOption.IGNORE_CASE)),
        RegexValueParser("E(\\d+)".toRegex()),
        RegexValueParser("Episode (\\d+)".toRegex()),
        CommonEpisodeValueParser,
        // 连续数字只出现过一次的
        RegexValueParser("^\\D*?(\\d+)\\D*?\$".toRegex()),
        RegexValueParser("#(\\d+)".toRegex()),
    )

    private val textClear = TextClear(
        mapOf(
            Regex("(?:480|720|1080|2160)P", RegexOption.IGNORE_CASE) to "",
            Regex("(1920x1080|3840x2160)", RegexOption.IGNORE_CASE) to "",
            Regex("x(?:264|265)", RegexOption.IGNORE_CASE) to "",
            Regex("flacx2|ma10p|hi10p|yuv420p10|10bit|hevc10|aacx2|flac|_", RegexOption.IGNORE_CASE) to "",
            Regex("4k", RegexOption.IGNORE_CASE) to "",
            Regex("\\b[A-Fa-f0-9]{8}\\b", RegexOption.IGNORE_CASE) to "",
        )
    )

    override fun createSourceGroup(sourceItem: SourceItem): SourceItemGroup {
        return FunctionalItemGroup { file ->
            val string = textClear.input(file.path.nameWithoutExtension)
            val episode = parserChain.firstNotNullOfOrNull {
                log.debug("Parser {}", it)
                it.parse(string)
            }

            val vars = MapPatternVariables()
            padNumber(episode)?.run {
                vars.addVariable("episode", this)
            }
            UniversalFileVariable(vars)
        }
    }

    override fun support(item: SourceItem): Boolean = true

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