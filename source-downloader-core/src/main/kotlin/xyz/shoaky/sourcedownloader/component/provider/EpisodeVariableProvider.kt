package xyz.shoaky.sourcedownloader.component.provider

import org.apache.commons.lang3.math.NumberUtils
import xyz.shoaky.sourcedownloader.sdk.*
import xyz.shoaky.sourcedownloader.sdk.component.VariableProvider
import kotlin.io.path.nameWithoutExtension

object EpisodeVariableProvider : VariableProvider {

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
        Regex("\\[(?!\\d+])[^\\[\\]]*?(.*?)]") to ""
    )

    override fun createSourceGroup(sourceItem: SourceItem): SourceItemGroup {
        return FunctionalItemGroup { path ->
            val filename = path.nameWithoutExtension
            val replace = pattern.replace(replace(filename)) { "${it.value.trim()} " }
            val episode = replace.split(" ")
                .filter { it.isNotBlank() }
                .map { it.removePrefix("[").removeSuffix("]") }
                .filter { NumberUtils.isParsable(it) }
                .filter { it.length > 1 }
                .maxByOrNull { it.length }
                ?.let {
                    if (it.contains(".")) {
                        it.toFloat()
                    } else {
                        it.toInt()
                    }
                }
            val vars = MapPatternVariables()
            padNumber(episode)?.run {
                vars.addVariable("episode", this)
            }
            UniversalSourceFile(vars)
        }
    }

    override fun support(item: SourceItem): Boolean = true

    private fun replace(filename: String): String {
        var res = filename
        replaces.forEach { (key, value) ->
            res = res.replace(key, value)
        }
        return res
    }

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