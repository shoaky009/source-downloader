package io.github.shoaky.sourcedownloader.common.anime.extractor

interface Extractor {

    fun extract(raw: String): List<String>?
}