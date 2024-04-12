package io.github.shoaky.sourcedownloader.external.season

import com.dgtlrepublic.anitomyj.AnitomyJ
import com.dgtlrepublic.anitomyj.Element

object ExtractTitleSeasonParser : SeasonParser {

    override fun input(subject: String): SeasonResult? {
        return AnitomyJ.parse(subject)
            .filter { it.category == Element.ElementCategory.kElementAnimeTitle }
            .map {
                LastStringSeasonParser.input(it.value)
            }.firstOrNull()
    }
}