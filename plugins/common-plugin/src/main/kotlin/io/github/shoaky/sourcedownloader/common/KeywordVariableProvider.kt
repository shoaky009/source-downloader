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
    private val regexPattern: String = "[()\\[](@keyword)[()\\]]",
) : VariableProvider {

    private val words = buildList {
        addAll(keywords)
        addAll(keywordsFile?.readLines() ?: emptyList())
    }.map {
        // "keyword" 匹配的字符串
        // "mode" 0:默认 [value]不包含括号时如果前后没有括号不则不匹配, [value]包含括号时则不检查括号
        // "alias" 如果不为空优先使用该值作为匹配结果的变量
        // "keyword|mode|alias"
        val split = it.split("|")
        val word = split.first()
        val mode = split.getOrElse(1) { "0" }
        val alias = split.getOrElse(2) { null }
        Word(word, mode.toIntOrNull() ?: 0, alias)
    }.toSet()

    override fun createItemGroup(sourceItem: SourceItem): SourceItemGroup {
        val title = sourceItem.title
        val matchedWord = words.firstOrNull { word ->
            if (word.matchTitleMode == 1) {
                return@firstOrNull title.contains(word.value, ignoreCase = true)
            }

            val regex = regexPattern.replace("@keyword", word.value)
                .toRegex(RegexOption.IGNORE_CASE)
            regex.find(title) != null
        }
        log.info("Keyword $matchedWord match: $title")

        val variables = matchedWord?.let {
            val word = it.alias ?: it.value
            MapPatternVariables(
                mapOf("keyword" to word)
            )
        } ?: PatternVariables.EMPTY
        return SourceItemGroup.shared(variables)
    }

    override fun support(sourceItem: SourceItem): Boolean {
        return true
    }

    companion object {

        private val log = LoggerFactory.getLogger(KeywordVariableProvider::class.java)

    }
}

private data class Word(
    val value: String,
    /**
     * 0:默认 [value]不包含括号时如果前后没有括号不则不匹配, [value]包含括号时则不检查括号
     * 1:只要contains则匹配
     */
    val matchTitleMode: Int,
    val alias: String?
)