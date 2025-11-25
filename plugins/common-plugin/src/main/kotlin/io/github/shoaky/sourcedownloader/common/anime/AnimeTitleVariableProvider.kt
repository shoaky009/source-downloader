package io.github.shoaky.sourcedownloader.common.anime

import com.google.common.base.CharMatcher
import io.github.shoaky.sourcedownloader.common.anime.extractor.*
import io.github.shoaky.sourcedownloader.sdk.PatternVariables
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.VariableProvider
import io.github.shoaky.sourcedownloader.sdk.util.TextClear
import org.slf4j.LoggerFactory
import java.util.function.Predicate

object AnimeTitleVariableProvider : VariableProvider {

    private val inputClear = TextClear(
        mapOf(
            Regex("（僅限港澳台）") to "",
            Regex("10-bit|8-bit|1080p|720p|HEVC|BDRip|AV1|OPUS|AVC", RegexOption.IGNORE_CASE) to "",
            Regex("(GB|BIG5).?MP4|\\d+X\\d+|\\d\\.0|\\d+-\\d+", RegexOption.IGNORE_CASE) to "",
            Regex("[(【（]") to "[",
            Regex("[)】）]") to "]",
            Regex("\\d+月新番|\\[\\d+]|\\[END]|\\[\\d*v\\d+]|★.*?★", RegexOption.IGNORE_CASE) to "",
            Regex("\\[[^]]*(简|繁|招募|翻译)[^]]*]") to "",
            Regex("\\[]", RegexOption.IGNORE_CASE) to "",
            Regex("\\|\\s*\$", RegexOption.IGNORE_CASE) to "",
        )
    )

    private val defaultChain: List<Extractor> = listOf(
        AniTitleExtractor,
        SeparateTitleExtractor(" / "),
        SeparateTitleExtractor(" | "),
        SeparateTitleExtractor("\\"),
    )

    private val fallbackChain: List<Extractor> = listOf(
        SeparateTitleExtractor("/"),
        SeparateTitleExtractor("|"),
        AllBracketTitleExtractor,
        DefaultTitleExtractor
    )
    private val titlesFilter: List<Predicate<String>> = listOf(
        Predicate { it.contains("字幕组") },
    )

    override fun itemVariables(sourceItem: SourceItem): PatternVariables {
        val rawTitle = inputClear.input(sourceItem.title).trim()
        log.debug("Text to extract: {}", rawTitle)
        return chain(rawTitle, defaultChain) ?: PatternVariables.EMPTY
    }

    private fun chain(rawTitle: String, extractors: List<Extractor>): Titles? {
        for (extractor in extractors) {
            val titles = extractor.extract(rawTitle) ?: continue
            if (titles.isEmpty()) {
                continue
            }
            log.debug("Extractor:{} Extracted titles: {}", extractor::class.simpleName, titles)
            val processedTitles = titles
                .map { title ->
                    val result = title.replace(bracketsRegex, "").trim()
                    if (result.isEmpty() && extractors !== fallbackChain) {
                        return chain(rawTitle, fallbackChain)
                    }
                    result
                }
                .filter { title -> titlesFilter.none { filter -> filter.test(title) } }
            if (processedTitles.size == 1) {
                return Titles(title = processedTitles.first())
            }

            val romajiTitle = findRomajiTitle(processedTitles)
            if (romajiTitle != null) {
                val title = processedTitles.firstOrNull { it != romajiTitle } ?: romajiTitle
                return Titles(title = title, romajiTitle = romajiTitle)
            }
        }
        return chain(rawTitle, fallbackChain)
    }

    private fun findRomajiTitle(removed: List<String>): String? {
        for (str in removed) {
            val allOf = CharMatcher.ascii().matchesAllOf(str)
            if (allOf) {
                return str
            }
        }
        return null
    }

    override fun primaryVariableName(): String {
        return "title"
    }

    private val log = LoggerFactory.getLogger(AnimeTitleVariableProvider::class.java)

}

val bracketsRegex = Regex("\\[.*?]")