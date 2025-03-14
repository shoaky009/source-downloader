package io.github.shoaky.sourcedownloader.common.anime

import com.dgtlrepublic.anitomyj.AnitomyJ
import com.dgtlrepublic.anitomyj.Element
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import io.github.shoaky.sourcedownloader.external.anilist.AnilistClient
import io.github.shoaky.sourcedownloader.external.anilist.Search
import io.github.shoaky.sourcedownloader.external.anilist.Title
import io.github.shoaky.sourcedownloader.external.bangumi.BgmTvApiClient
import io.github.shoaky.sourcedownloader.external.bangumi.SearchSubjectRequest
import io.github.shoaky.sourcedownloader.external.bangumi.SubjectItem
import io.github.shoaky.sourcedownloader.sdk.PatternVariables
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.VariableProvider
import io.github.shoaky.sourcedownloader.sdk.util.TextClear
import me.xdrop.fuzzywuzzy.FuzzySearch
import org.slf4j.LoggerFactory
import kotlin.io.path.name

/**
 * 从SourceItem.title中提取和清洗标题给anilist或bgmtv进行搜索获取对应元数据，
 * 会自动根据title中的语言来决定用哪个网站进行搜索
 */
// TODO 重构成可扩展的
class AnimeVariableProvider(
    private val bgmTvApiClient: BgmTvApiClient,
    private val anilistClient: AnilistClient,
    private val preferBgmTv: Boolean = false
) : VariableProvider {

    private val searchCache =
        CacheBuilder.newBuilder().maximumSize(500).build(object : CacheLoader<String, Anime>() {
            override fun load(title: String): Anime {
                return searchAnime(title)
            }
        })

    override fun itemVariables(sourceItem: SourceItem): PatternVariables {
        return create(sourceItem)
    }

    override fun fileVariables(
        sourceItem: SourceItem,
        itemVariables: PatternVariables,
        sourceFiles: List<SourceFile>
    ): List<PatternVariables> {
        return sourceFiles.map {
            resolveFromFile(sourceItem, itemVariables, it)
        }
    }

    private fun resolveFromFile(
        sourceItem: SourceItem,
        itemVariables: PatternVariables,
        sourceFile: SourceFile
    ): PatternVariables {
        if (sourceFile.path.isAbsolute) {
            return PatternVariables.EMPTY
        }
        // 获取第二级，并且还要进行一些过滤
        val targetPathIndex = 1
        val subPath = sourceFile.path.toList().getOrNull(targetPathIndex)
        if (subPath == sourceFile.path.fileName || subPath == null) {
            return PatternVariables.EMPTY
        }
        val name = subPath.name
        // 先简单过滤后面根据情况添加
        if (name.length < 10) {
            return PatternVariables.EMPTY
        }

        val title = extractTitle(name)
        return searchCache[title]
    }

    private fun create(sourceItem: SourceItem): Anime {
        val title = extractTitle(sourceItem.title)
        return searchCache[title]
    }

    /**
     * 根据标题来决定用bgm还是anilist来进行第一次搜索
     */
    private fun searchAnime(title: String): Anime {
        val hasJp = hasLanguage(title, Character.UnicodeScript.HIRAGANA, Character.UnicodeScript.KATAKANA)
        val hasChinese = hasLanguage(title, Character.UnicodeScript.HAN)
        var anilistResult: Title? = null
        if (hasJp || hasChinese.not()) {
            val response = anilistClient.execute(Search(title)).body()
            if (response.errors.isNotEmpty()) {
                return Anime()
            }
            val anime = response.data.page.medias.firstOrNull()
            if (anime == null) {
                log.warn("anilist searching anime: $title no result")
            }
            anilistResult = anime?.title
        }
        if (preferBgmTv.not() && anilistResult != null) {
            return Anime(
                anilistResult.romaji,
                anilistResult.native
            )
        }

        val request = SearchSubjectRequest(anilistResult?.native ?: title)
        val body = bgmTvApiClient.execute(
            request
        ).body()

        if (body.list.isEmpty()) {
            log.warn("bgmtv searching anime: $title no result")
            return Anime()
        }
        val subjectItem = body.list.getHighestScoreSubjectItem(request.keyword)

        if (anilistResult != null) {
            return Anime(
                anilistResult.romaji,
                subjectItem.name
            )
        }
        // 这里是中文的情况
        val response = anilistClient.execute(
            Search(subjectItem.name)
        ).body()
        if (response.errors.isNotEmpty()) {
            return Anime(
                nativeName = subjectItem.name
            )
        }
        val media = response.data.page.medias.firstOrNull()
        if (media == null) {
            log.warn("anilist searching anime: $title no result")
        }

        return Anime(
            romajiName = media?.title?.romaji,
            nativeName = subjectItem.name
        )
    }

    private fun List<SubjectItem>.getHighestScoreSubjectItem(keyword: String): SubjectItem {
        val hasJp = hasLanguage(keyword, Character.UnicodeScript.HIRAGANA, Character.UnicodeScript.KATAKANA)
        val hasChinese = hasLanguage(keyword, Character.UnicodeScript.HAN)
        val choices = if (hasJp || hasChinese.not()) {
            this.map { it.name }
        } else {
            this.map { it.nameCn }
        }
        val result = FuzzySearch.extractOne(keyword, choices)
        log.debug("Get highest score subject item: {} -- {}", this[result.index], keyword)
        return this[result.index]
    }

    private fun hasLanguage(text: String, vararg unicode: Character.UnicodeScript): Boolean {
        return text.codePoints().anyMatch {
            unicode.contains(Character.UnicodeScript.of(it))
        }
    }

    override fun extractFrom(sourceItem: SourceItem, text: String): PatternVariables? {
        return searchCache.get(text)
    }

    override fun primary(): String {
        return "nativeName"
    }

    companion object {

        private val textClear = TextClear(
            mapOf(
                Regex("\\d{2}-\\d{2}|全\\d+话|全\\d+話") to "",
                Regex("\\+OVA|\\+OAD") to "",
                Regex("[(【（]") to "[",
                Regex("[)】）]") to "]",
                Regex("[。，～]") to " ",
                Regex("[~！～+]") to "",
                Regex(" - ") to " ",
                Regex("Special|SP|TV|S01|Season 1|Season 01|BDBOX|BD-BOX") to "",
                Regex("S(\\d+)") to "Season $1",
            )
        )
        private val bracketsRegex = Regex("\\[.*?]")

        fun extractTitle(rawTitle: String): String {
            val text = textClear.input(rawTitle)
            val removedBucket = text.replace(bracketsRegex, "").trim()
            if (removedBucket.length > 12) {
                val sp = listOf("/", "|").firstOrNull {
                    removedBucket.contains(it)
                }

                if (sp == null) {
                    val blanksRegex = "\\s{2,}".toRegex()
                    val matchResult = blanksRegex.find(removedBucket)
                    if (matchResult != null) {
                        return removedBucket.substring(0, matchResult.range.first)
                    }
                }

                if (sp == null) {
                    return removedBucket
                }

                // 优先选择日语，最后是中文尽可能用anilist搜索
                val title = removedBucket.split(sp)
                    .map { TitleScore(it) }
                    .maxBy { it.score }.title

                return AnitomyJ.parse(title)
                    .firstOrNull { it.category == Element.ElementCategory.kElementAnimeTitle }?.value
                    ?: title
            }
            if (removedBucket.isNotBlank()) {
                return removedBucket
            }
            val matches = bracketsRegex.findAll(text).toList()
            if (matches.size == 1) {
                return matches[0].value.removePrefix("[").removeSuffix("]")
            }
            if (matches.size > 1) {
                return matches[1].value.removePrefix("[").removeSuffix("]")
            }
            return text
        }
    }

}

private class TitleScore(
    val title: String,
) {

    val score: Int = byLanguage(title)

    private fun byLanguage(title: String): Int {
        return title.codePoints().map {
            characterScores.getOrDefault(Character.UnicodeScript.of(it), 1)
        }.sum()
    }

    companion object {

        private val characterScores = mapOf(
            Character.UnicodeScript.HAN to 1,
            Character.UnicodeScript.HIRAGANA to 10,
            Character.UnicodeScript.KATAKANA to 10,
        )
    }
}

private val log = LoggerFactory.getLogger(AnimeVariableProvider::class.java)

data class Anime(
    val romajiName: String? = null,
    val nativeName: String? = null
) : PatternVariables {

    override fun variables(): Map<String, String> {
        return mapOf(
            "romajiName" to romajiName.orEmpty(),
            "nativeName" to nativeName.orEmpty()
        )
    }
}