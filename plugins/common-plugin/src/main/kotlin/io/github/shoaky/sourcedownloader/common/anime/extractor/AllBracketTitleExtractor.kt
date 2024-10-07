package io.github.shoaky.sourcedownloader.common.anime.extractor

object AllBracketTitleExtractor : Extractor {

    private val BRACKET_REGEX = Regex("\\[(.*?)]")

    override fun extract(raw: String): List<String>? {
        val isNotAllBracket = raw.replace(BRACKET_REGEX, "").isNotBlank()
        if (isNotAllBracket) {
            return null
        }
        val titles = BRACKET_REGEX.findAll(raw).map { it.groupValues.last() }.toList()
        return titles
    }
}