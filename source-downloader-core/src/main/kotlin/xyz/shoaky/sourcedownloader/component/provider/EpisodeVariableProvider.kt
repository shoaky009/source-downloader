package xyz.shoaky.sourcedownloader.component.provider

import org.apache.commons.lang3.math.NumberUtils
import xyz.shoaky.sourcedownloader.sdk.*
import xyz.shoaky.sourcedownloader.sdk.component.VariableProvider
import kotlin.io.path.nameWithoutExtension

object EpisodeVariableProvider : VariableProvider {

    private val parserChain: List<ValueParser> = listOf(
        RegexValueParser(Regex("第(\\d+)话")),
        RegexValueParser(Regex("第(\\d+)集")),
        RegexValueParser(Regex("第(\\d+)話")),
        RegexValueParser(Regex("(\\d+)話")),
        RegexValueParser(Regex("(\\d+)话")),
        RegexValueParser("EP(\\d+)".toRegex(RegexOption.IGNORE_CASE)),
        RegexValueParser("S(\\d+)E(\\d+)".toRegex(RegexOption.IGNORE_CASE)),
        CommonEpisodeValueParser,
        // 连续数字只出现过一次的
        RegexValueParser("^\\D*?(\\d+)\\D*?\$".toRegex()),
    )

    override fun createSourceGroup(sourceItem: SourceItem): SourceItemGroup {
        return FunctionalItemGroup { path ->
            val filename = path.nameWithoutExtension
            val episode = parserChain.firstNotNullOfOrNull { it.parse(filename) }

            val vars = MapPatternVariables()
            padNumber(episode)?.run {
                vars.addVariable("episode", this)
            }
            UniversalSourceFile(vars)
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
        return regex.find(value)?.groupValues?.lastOrNull()?.toInt()
    }
}

private object CommonEpisodeValueParser : ValueParser {

    private val pattern = Regex("(\\[?[^\\[\\]]*]?)")

    private val replaces = mapOf(
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
            .filter { NumberUtils.isParsable(it) && it.length > 1 }
            .maxByOrNull { it.length }
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