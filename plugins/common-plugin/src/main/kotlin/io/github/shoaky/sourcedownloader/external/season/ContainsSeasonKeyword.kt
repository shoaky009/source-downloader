package io.github.shoaky.sourcedownloader.external.season

object ContainsSeasonKeyword : SeasonParser {

    /**
     * 只需要被空格包围的
     */
    private val keywords =
        listOf(" II ", " III ", " IV ", " V ", " VI ", " VII ", " VIII ", " IX ", " X ", " Ⅲ ", " Ⅱ ", " Ⅳ ")

    override fun input(subject: String): SeasonResult? {
        for (keyword in keywords) {
            if (subject.contains(keyword).not()) {
                continue
            }

            GeneralSeasonParser.seasonNumberMapping[keyword.trim()]?.let {
                return SeasonResult(it, SeasonResult.Accuracy.ACCURATE)
            }
        }
        return null
    }
}