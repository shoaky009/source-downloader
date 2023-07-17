package io.github.shoaky.sourcedownloader.common.anime

import com.dgtlrepublic.anitomyj.AnitomyJ
import com.dgtlrepublic.anitomyj.Element
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import io.github.shoaky.sourcedownloader.external.anilist.AnilistClient
import io.github.shoaky.sourcedownloader.external.anilist.Search
import io.github.shoaky.sourcedownloader.external.anilist.Title
import io.github.shoaky.sourcedownloader.external.bangumi.BgmTvApiClient
import io.github.shoaky.sourcedownloader.external.bangumi.SearchSubjectV0Request
import io.github.shoaky.sourcedownloader.sdk.*
import io.github.shoaky.sourcedownloader.sdk.component.VariableProvider
import io.github.shoaky.sourcedownloader.sdk.util.replaces
import org.slf4j.LoggerFactory

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

    override fun createSourceGroup(sourceItem: SourceItem): SourceItemGroup {
        val create = create(sourceItem)
        return AnimeSourceGroup(create)
    }

    override fun support(item: SourceItem): Boolean = true

    private fun create(sourceItem: SourceItem): Anime {
        val title = extractTitle(sourceItem)
        return searchCache.get(title)
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

        val body = bgmTvApiClient.execute(
            SearchSubjectV0Request(anilistResult?.native ?: title)
        ).body()
        val subjectItem = body.data.firstOrNull()
        if (subjectItem == null) {
            log.warn("bgmtv searching anime: $title no result")
            return Anime()
        }

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

    private fun extractTitle(sourceItem: SourceItem): String {
        val text = sourceItem.title.replaces(
            leftBrReplaces, "["
        ).replaces(
            rightBrReplaces, "]"
        ).replaces(
            emptyReplaces, "", false
        ).replaces(
            bReplaces, " "
        ).replace(Regex("S(\\d+)"), "Season $1")

        val removedBucket = text.replace(bracketsRegex, "").trim()
        if (removedBucket.length > 12) {
            val sp = listOf("/", "|").firstOrNull {
                removedBucket.contains(it)
            } ?: return removedBucket

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

    private fun hasLanguage(text: String, vararg unicode: Character.UnicodeScript): Boolean {
        return text.codePoints().anyMatch {
            unicode.contains(Character.UnicodeScript.of(it))
        }
    }

    companion object {

        private val leftBrReplaces = listOf("(", "【", "（")
        private val rightBrReplaces = listOf(")", "】", "）")
        private val emptyReplaces = listOf(
            "~", "！", "～", "Special", "SP", "TV", "-",
            "S01", "Season 1", "Season 01",
            "BDBOX", "BD-BOX", "+", "OVA"
        )
        private val bReplaces = listOf(
            "。", "，", "～"
        )
        private val bracketsRegex = Regex("\\[.*?]")
    }

}

private class TitleScore(
    val title: String,
) {

    val score: Int = byLanguage(title)

    private fun byLanguage(title: String): Int {
        return title.codePoints().map {
            when (Character.UnicodeScript.of(it)) {
                Character.UnicodeScript.HAN -> 1
                Character.UnicodeScript.HIRAGANA -> 10
                Character.UnicodeScript.KATAKANA -> 10
                else -> 1
            }
        }.sum()
    }
}

private val log = LoggerFactory.getLogger(AnimeSourceGroup::class.java)

internal class AnimeSourceGroup(
    private val anime: Anime,
) : SourceItemGroup {

    override fun sharedPatternVariables(): PatternVariables {
        return anime
    }

    override fun filePatternVariables(paths: List<SourceFile>): List<FileVariable> {
        return paths.map { FileVariable.EMPTY }
    }

}

internal data class Anime(
    val romajiName: String? = null,
    val nativeName: String? = null
) : PatternVariables