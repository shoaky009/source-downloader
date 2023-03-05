package xyz.shoaky.sourcedownloader.mikan.parse

import com.dgtlrepublic.anitomyj.AnitomyJ
import com.dgtlrepublic.anitomyj.Element
import org.apache.commons.lang3.math.NumberUtils

internal object AnitomEpisodeParser : ValueParser {

    override val name: String = "Anitomy"

    override fun apply(subjectContent: SubjectContent, filename: String): Result {
        val elements = AnitomyJ.parse(filename).associateBy({ it.category }, { it.value })
//        val season = elements[Element.ElementCategory.kElementAnimeSeason]
        val episode = elements[Element.ElementCategory.kElementEpisodeNumber]
        if (NumberUtils.isParsable(episode)) {
            return Result(episode)
        }
        return Result()
    }

}