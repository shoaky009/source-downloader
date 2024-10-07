package io.github.shoaky.sourcedownloader.common.anime.extractor

object DefaultTitleExtractor : Extractor {

    override fun extract(raw: String): List<String> {
        return listOf(raw)
    }

}