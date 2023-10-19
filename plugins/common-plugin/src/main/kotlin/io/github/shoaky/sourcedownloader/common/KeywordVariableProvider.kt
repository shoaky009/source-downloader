package io.github.shoaky.sourcedownloader.common

import io.github.shoaky.sourcedownloader.sdk.MapPatternVariables
import io.github.shoaky.sourcedownloader.sdk.PatternVariables
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.SourceItemGroup
import io.github.shoaky.sourcedownloader.sdk.component.VariableProvider
import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.io.path.readLines

/**
 *
 */
class KeywordVariableProvider(
    private val keywords: List<String> = emptyList(),
    private val keywordsFile: Path? = null,
    private val prefixes: List<Char> = listOf('(', '['),
    private val suffixes: List<Char> = listOf(')', ']')
) : VariableProvider {

    override fun createSourceGroup(sourceItem: SourceItem): SourceItemGroup {
        val words = buildList {
            addAll(keywords)
            addAll(keywordsFile?.readLines() ?: emptyList())
        }.map {
            val split = it.split("|")
            val word = split.first()
            val mode = split.getOrElse(1) { "0" }
            Word(word, mode.toInt())
        }.toSet()

        val title = sourceItem.title
        val result = words.match(title).firstOrNull {
            if (it.word.matchTitleMode == 1) {
                return@firstOrNull true
            }

            defaultTitleMatch(it, title)
        }?.word?.value
        log.info("Keyword $result match: $title")

        val variables = result?.let {
            MapPatternVariables(
                mapOf("keyword" to it)
            )
        } ?: PatternVariables.EMPTY
        return SourceItemGroup.shared(variables)
    }

    override fun support(item: SourceItem): Boolean {
        return true
    }

    private fun Set<Word>.match(title: String): List<MatchedResult> {
        return this.mapNotNull {
            val index = title.indexOf(it.value, ignoreCase = true)
            if (index < 0) {
                return@mapNotNull null
            }
            MatchedResult(it, index..(index + it.value.length))
        }
    }

    private fun defaultTitleMatch(it: MatchedResult, title: String): Boolean {
        if (it.word.containsPairs(prefixes, suffixes)) {
            return true
        }
        val beginStr = title.getOrNull(it.range.first - 1)
        val endStr = title.getOrNull(it.range.last)
        return prefixes.contains(beginStr) && suffixes.contains(endStr)
    }

    companion object {

        private val log = LoggerFactory.getLogger(KeywordVariableProvider::class.java)
        private fun Word.containsPairs(prefixes: List<Char>, suffixes: List<Char>): Boolean {
            return value.any { prefixes.contains(it) } and value.any { suffixes.contains(it) }
        }
    }
}

private data class MatchedResult(
    val word: Word,
    val range: IntRange
)

private data class Word(
    val value: String,
    /**
     * 0:默认 [value]不包含括号时如果前后没有括号不则不匹配, [value]包含括号时则不检查括号
     * 1:只要contains则匹配
     */
    val matchTitleMode: Int
)