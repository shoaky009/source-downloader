package xyz.shoaky.sourcedownloader.mikan.parse

import org.apache.commons.lang3.math.NumberUtils

object EpisodeParser : ValueParser {
    override val name: String = "EpisodeParser"

    override fun apply(subjectContent: SubjectContent, filename: String): Result {
        val idk = replace(filename)
        val episode = idk.split(" ")
            .filter { it.isNotBlank() }
            .filter { NumberUtils.isParsable(it) }
            .map {
                if (it.contains(".")) {
                    it.toFloat()
                } else {
                    it.toInt()
                }
            }
            .firstOrNull { subjectContent.subject.totalEpisodes >= it.toFloat() }
        return Result(episode)
    }

    private fun replace(filename: String): String {
        var res = filename
        replaces.forEach { (key, value) ->
            res = res.replace(key, value)
        }
        return res
    }

    private val replaces = mapOf(
        Regex("【") to "[",
        Regex("】") to "]",
        Regex("\\(") to "[",
        Regex("\\)") to "]",
        //匹配[any]但是除了纯数字的内容
        Regex("\\[(?!\\d+])[^\\[\\]]*?(.*?)]") to ""
    )
}