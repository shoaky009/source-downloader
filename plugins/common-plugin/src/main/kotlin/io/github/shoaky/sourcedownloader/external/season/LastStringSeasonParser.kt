package io.github.shoaky.sourcedownloader.external.season

import org.apache.commons.lang3.CharUtils

object LastStringSeasonParser : SeasonParser {

    private val lastMatchPattern =
        "((?<!\\d)\\d(?!\\d)(?!.*\\d)|二|三|四|五|六|七|八|九|十|II|III|IV|V|VI|VII|VIII|IX|X|Ⅱ|Ⅲ|Ⅳ|[２-９])$".toRegex()
    private val excludePattern = Regex("(\\d+[/／]\\d+)$")

    override fun input(subject: String): SeasonResult? {
        if (excludePattern.containsMatchIn(subject)) {
            return null
        }
        val matcher = lastMatchPattern.find(subject) ?: return null
        if (matcher.range.first > 0) {
            val prevChar = subject[matcher.range.first - 1]
            if (CharUtils.isAsciiAlpha(prevChar) && !subject.substring(0, matcher.range.first)
                    .endsWith("Season", true)
            ) {
                return null
            }
        }
        val value = matcher.value.toIntOrNull() ?: GeneralSeasonParser.seasonNumberMapping[matcher.value]
        return SeasonResult(value, SeasonResult.Accuracy.ACCURATE)
    }
}