package io.github.shoaky.sourcedownloader.common.anime.extractor

class SeparateTitleExtractor(
    private val separate: String
) : Extractor {

    private val bracketRegex = Regex("\\[(.*?)]")

    override fun extract(raw: String): List<String>? {
        val isNotAllBracket = raw.replace(bracketRegex, "").isNotBlank()
        if (isNotAllBracket) {
            return extractNoBrackets(raw)
        }

        val target = bracketRegex.findAll(raw).map { it.groupValues.last() }
            .filter { it.contains(separate) }
            .maxByOrNull { it.length } ?: return null
        return extractNoBrackets(target)
    }

    private fun extractNoBrackets(raw: String): List<String>? {
        val split = raw.split(separate)
        if (split.size == 1) {
            return null
        }
        return split
    }

}