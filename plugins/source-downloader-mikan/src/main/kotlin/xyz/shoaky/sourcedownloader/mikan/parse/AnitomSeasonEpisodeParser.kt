package xyz.shoaky.sourcedownloader.mikan.parse

import com.dgtlrepublic.anitomyj.AnitomyJ
import com.dgtlrepublic.anitomyj.Element

internal class AnitomSeasonEpisodeParser : SeasonEpisodeParser {

    override val name: String = "Anitomy"

    override fun apply(subjectContent: SubjectContent, filename: String): Result {
        val elements = AnitomyJ.parse(filename).associateBy({ it.category }, { it.value })
        val season = elements[Element.ElementCategory.kElementAnimeSeason]
        val episode = elements[Element.ElementCategory.kElementEpisodeNumber]
        return Result(season?.toInt(), episode?.toInt())
    }

}