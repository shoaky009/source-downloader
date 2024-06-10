package io.github.shoaky.sourcedownloader.common

import com.optimaize.langdetect.LanguageDetectorBuilder
import com.optimaize.langdetect.ngram.NgramExtractors
import com.optimaize.langdetect.profiles.LanguageProfileReader
import io.github.shoaky.sourcedownloader.sdk.MapPatternVariables
import io.github.shoaky.sourcedownloader.sdk.PatternVariables
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.VariableProvider
import io.github.shoaky.sourcedownloader.sdk.util.replaces
import kotlin.io.path.extension
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.notExists
import kotlin.io.path.readLines

/**
 * 通过文件名中的语言标识符来提供语言变量, 或读取文件内容如果存在的话
 * 当前支持的语言
 * zh-CHS
 * zh-CHT
 */
class LanguageVariableProvider(
    private val readContent: Boolean = true
) : VariableProvider {

    override fun itemVariables(sourceItem: SourceItem): PatternVariables = PatternVariables.EMPTY

    override fun fileVariables(
        sourceItem: SourceItem,
        itemVariables: PatternVariables,
        sourceFiles: List<SourceFile>,
    ): List<PatternVariables> {
        return sourceFiles.map { file ->
            val name = file.path.nameWithoutExtension.replaces(replaces, " ")
            val language = languages.entries.firstOrNull { (regex, _) -> regex.containsMatchIn(name) }?.value
            if (language == null && readContent) {
                return@map fromFile(file)
            }
            if (language == null) {
                return@map PatternVariables.EMPTY
            }
            MapPatternVariables(mapOf("language" to language))
        }
    }

    override fun primary(): String {
        return "language"
    }

    private fun fromFile(file: SourceFile): PatternVariables {
        if (file.path.notExists()) {
            return PatternVariables.EMPTY
        }
        val extension = file.path.extension
        val collected: List<String> = when (extension) {
            "ass" -> {
                collectAssFormatText(file)
            }

            "srt" -> {
                collectSrtFormatText(file)
            }

            else -> emptyList()
        }
        val result = languageDetector.detect(
            collected.joinToString()
        ).orNull() ?: return PatternVariables.EMPTY
        val language = iso6391mapping[result.toString()] ?: return PatternVariables.EMPTY
        return MapPatternVariables(mapOf("language" to language))
    }

    private fun collectAssFormatText(file: SourceFile): List<String> {
        // 暂时没处理OP在前面的情况
        return file.path.readLines()
            .asSequence()
            .dropWhile { it.trim() != "[Events]" }
            .filter { it.startsWith("Dialogue") }
            .mapNotNull { line -> line.split(",").lastOrNull().takeIf { it.isNullOrBlank().not() } }
            .take(DEFAULT_COLLECT_LINES).toList()
    }

    private fun collectSrtFormatText(file: SourceFile): List<String> {
        return file.path.readLines()
            .asSequence()
            .filter { NUMBER_REGEX.matches(it).not() && SRT_TIME_REGEX.matches(it).not() }
            .filter { it.isNotBlank() }
            .take(DEFAULT_COLLECT_LINES).toList()
    }

    companion object {

        private val replaces = listOf("-", "_", "[", "]", "(", ")", ".")
        private val languages = mapOf(
            " chs|[ .]sc".toRegex(RegexOption.IGNORE_CASE) to "zh-CHS",
            " cht|[ .]tc".toRegex(RegexOption.IGNORE_CASE) to "zh-CHT",
            "jpsc".toRegex(RegexOption.IGNORE_CASE) to "zh-CHS",
            " gb".toRegex(RegexOption.IGNORE_CASE) to "zh-CHS",
            " big5".toRegex(RegexOption.IGNORE_CASE) to "zh-CHT",
        )
        private val NUMBER_REGEX: Regex = "(\\d+)".toRegex()
        private val SRT_TIME_REGEX: Regex = "(\\d{2}:\\d{2}:\\d{2},\\d{3}).*(\\d{2}:\\d{2}:\\d{2},\\d{3})".toRegex()
        private const val DEFAULT_COLLECT_LINES = 10

        private val iso6391mapping: Map<String, String> = mapOf(
            "zh-CN" to "zh-CHS",
            "zh-TW" to "zh-CHT",
            "zh-HK" to "zh-CHT",
            "zh-SG" to "zh-CHT",
        )

        // 后面需要跟着对象实例，暂时先偷懒写死有需要再说
        private val languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
            .withProfiles(LanguageProfileReader().read(listOf("zh-CN", "zh-TW", "ja")))
            .build()
    }

}