package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.sdk.MapPatternVariables
import io.github.shoaky.sourcedownloader.sdk.PatternVariables
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.SourceItemFilter
import io.github.shoaky.sourcedownloader.sdk.component.VariableProvider
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchService
import kotlin.io.path.readLines

/**
 *
 */
class KeywordIntegration(
    private val keywords: List<String> = emptyList(),
    private val keywordsFile: Path? = null,
    private val regexPattern: String = "[()\\[](@keyword)[()\\]]",
) : VariableProvider, SourceItemFilter, AutoCloseable {

    private var words: List<Word> = parseKeywords()
    private var stop: Boolean = false

    init {
        startWatchFile()
    }

    private fun parseKeywords(): List<Word> {
        return buildList {
            addAll(keywords)
            addAll(keywordsFile?.readLines() ?: emptyList())
        }.map {
            // "keyword" 匹配的字符串
            // "mode" 0:默认 [value]不包含括号时如果前后没有括号不则不匹配, [value]包含括号时则不检查括号
            // "alias" 如果不为空优先使用该值作为匹配结果的变量
            // "keyword|mode|alias"
            val split = it.split("|")
            val word = split.first()
            val mode = split.getOrElse(1) { "0" }.toIntOrNull() ?: 0
            val alias = split.getOrElse(2) { null }
            Word(word, mode, alias)
        }.distinct()
    }

    override fun itemVariables(sourceItem: SourceItem): PatternVariables {
        val title = sourceItem.title
        val matchedWord = matchWord(title)
        return matchedWord?.let {
            val word = it.alias ?: it.value
            MapPatternVariables(
                mapOf("keyword" to word)
            )
        } ?: PatternVariables.EMPTY
    }

    private fun matchWord(title: String): Word? {
        val matchedWord = words.firstOrNull { word ->
            if (word.matchTitleMode == 1) {
                return@firstOrNull title.contains(word.value, ignoreCase = true)
            }

            val regex = regexPattern.replace("@keyword", word.value)
                .toRegex(RegexOption.IGNORE_CASE)
            regex.find(title) != null
        }
        log.info("Keyword $matchedWord match: $title")
        return matchedWord
    }

    override fun extractFrom(sourceItem: SourceItem, text: String): PatternVariables? {
        val w = matchWord(text)
        val res = w?.alias ?: w?.value
        return res?.let {
            MapPatternVariables(
                mapOf("keyword" to it)
            )
        }
    }

    override fun primary(): String {
        return "keyword"
    }

    private fun startWatchFile() {
        if (keywordsFile == null) {
            return
        }
        val parent = keywordsFile.parent ?: return
        val watchService = try {
            keywordsFile.fileSystem.newWatchService()
        } catch (e: Exception) {
            log.error("Error creating watch service", e)
            return
        }
        parent.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY)

        val fileName = keywordsFile.fileName
        Thread.ofVirtual().start {
            log.info("Start watch {}", keywordsFile)
            while (!stop) {
                try {
                    handleEvent(watchService, fileName)
                } catch (e: Exception) {
                    log.error("Fail to reload keywords from {}", keywordsFile, e)
                }
            }
            watchService.close()
            log.info("Stop watch {}", keywordsFile)
        }
    }

    private fun handleEvent(watchService: WatchService, fileName: Path) {
        val key = watchService.take()
        for (event in key.pollEvents()) {
            if (event.kind() != StandardWatchEventKinds.ENTRY_MODIFY) {
                continue
            }
            val context = event.context()
            if (context !is Path) {
                continue
            }
            if (context == fileName) {
                log.info("Reload keywords from {}", keywordsFile)
                words = parseKeywords()
            }
        }
        key.reset()
    }

    override fun close() {
        stop = true
    }

    override fun test(t: SourceItem): Boolean {
        val title = t.title
        val matchedWord = matchWord(title)
        return matchedWord != null
    }

    companion object {

        private val log = LoggerFactory.getLogger(KeywordIntegration::class.java)

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