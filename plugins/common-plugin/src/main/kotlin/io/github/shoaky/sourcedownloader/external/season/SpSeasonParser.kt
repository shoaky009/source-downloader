package io.github.shoaky.sourcedownloader.external.season

object SpSeasonParser : SeasonParser {

    private val spRegexes = listOf(
        "OVA|OAD|SPs|S00|SP(\\d+)|SP (\\d+)".toRegex(),
        "Special|extra(\\d+)".toRegex(RegexOption.IGNORE_CASE),
        "特别篇|特別篇|\\[SP]|映像特典".toRegex(),
    )

    override fun input(subject: String): SeasonResult? {
        for (spRegex in spRegexes) {
            val matchResult = spRegex.find(subject)
            if (matchResult != null) {
                return SeasonResult(0, SeasonResult.Accuracy.ACCURATE)
            }
        }
        return null
    }
}
