package io.github.shoaky.sourcedownloader.external.season

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.optimaize.langdetect.LanguageDetectorBuilder
import com.optimaize.langdetect.ngram.NgramExtractors
import com.optimaize.langdetect.profiles.LanguageProfileReader
import io.github.shoaky.sourcedownloader.external.tmdb.GetTvShow
import io.github.shoaky.sourcedownloader.external.tmdb.SearchTvShow
import io.github.shoaky.sourcedownloader.external.tmdb.TmdbClient
import org.slf4j.LoggerFactory

/**
 * 从奇葩的季度命名中获取季度
 */
class TmdbSeasonParser(
    private val tmdbClient: TmdbClient,
    private val autoDetectLang: Boolean = false,
    private val language: String? = null,
) : SeasonParser {

    private val tmdbCache = CacheBuilder.newBuilder().maximumSize(500).build(
        object : CacheLoader<String, Int>() {
            override fun load(content: String): Int {
                val split = content.split(" ")
                val search = split.first()
                // 这里不需要lang因为tmdb支持多语言搜索
                val results = tmdbClient.execute(SearchTvShow(search)).body().results
                val firstSearchResult = results.firstOrNull() ?: return -1

                val lang = getLanguage(content)
                val tvShow = tmdbClient.execute(GetTvShow(firstSearchResult.id, lang)).body()
                if (tvShow.numberOfSeasons == 1) {
                    return 1
                }

                val last = split.last()
                return tvShow.seasons.map { it.copy(name = it.name.replace(" ", "")) }
                    .filter { it.name.contains(last) }
                    .map { it.seasonNumber }.firstOrNull() ?: -1
            }

            private fun getLanguage(content: String): String {
                if (autoDetectLang.not()) {
                    return language ?: "ja-jp"
                }
                val detect = languageDetector.detect(content).orNull() ?: return "ja-jp"
                log.debug("detect language:{} ,text:{}", detect.language, content)
                return detect.language
            }
        })

    override fun input(subject: String): SeasonResult? {
        val season = tmdbCache.get(subject)
        if (season < 1) {
            log.info("从TMDB获取季度失败name:{}", subject)
            return null
        }
        log.debug("从TMDB获取季度成功name:{},season:{}", subject, season)
        return SeasonResult(season, accuracy = SeasonResult.Accuracy.ACCURATE)
    }

    companion object {

        private val languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
            .withProfiles(LanguageProfileReader().read(listOf("zh-CN", "zh-TW", "ja")))
            .minimalConfidence(0.4)
            .build()
        private val log = LoggerFactory.getLogger(TmdbSeasonParser::class.java)
    }
}