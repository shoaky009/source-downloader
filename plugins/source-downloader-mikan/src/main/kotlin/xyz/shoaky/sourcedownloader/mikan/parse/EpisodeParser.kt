package xyz.shoaky.sourcedownloader.mikan.parse

import org.apache.commons.lang3.math.NumberUtils

object EpisodeParser : ValueParser {
    override val name: String = "EpisodeParser"

    private val pattern = Regex("(\\[?[^\\[\\]]*]?)")

    private val replaces = mapOf(
        Regex("【") to "[",
        Regex("】") to "]",
        Regex("\\(") to "[",
        Regex("\\)") to "]",
        Regex("(?<=\\d)集") to "",
        //匹配[any]但是除了纯数字的内容
        Regex("\\[(?!\\d+])[^\\[\\]]*?(.*?)]") to ""
    )

    override fun apply(subjectContent: SubjectContent, filename: String): Result {
        val replace = pattern.replace(replace(filename)) { "${it.value.trim()} " }
        val episode = replace.split(" ")
            .filter { it.isNotBlank() }
            .map { it.removePrefix("[").removeSuffix("]") }
            .filter { NumberUtils.isParsable(it) }
            .maxByOrNull { it.length }
            ?.let {
                if (it.contains(".")) {
                    it.toFloat()
                } else {
                    it.toInt()
                }
            }
        return Result(episode, Result.Accuracy.ACCURATE)
    }

    private fun replace(filename: String): String {
        var res = filename
        replaces.forEach { (key, value) ->
            res = res.replace(key, value)
        }
        return res
    }

}