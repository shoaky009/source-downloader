package io.github.shoaky.sourcedownloader.external.season

import io.github.shoaky.sourcedownloader.sdk.component.ComponentException

object LastStringSeasonParser : SeasonParser {

    private val lastMatchPattern = "(?:\\d+|二|三|四|五|六|七|八|九|十|II|III|IV|V|VI|VII|VIII|IX|X|Ⅱ|Ⅲ|Ⅳ)\$".toRegex()

    // 标题最后是连续数字的
    private val lastRegex = RegexRule(lastMatchPattern) {
        it.toIntOrNull() ?: GeneralSeasonParser.seasonNumberMapping[it]
        ?: throw ComponentException.processing("can't parse season number from $it")
    }

    override fun input(subject: String): SeasonResult? {
        lastRegex.ifMatchConvert(subject)?.let {
            return SeasonResult(it, SeasonResult.Accuracy.ACCURATE)
        }
        return null
    }
}