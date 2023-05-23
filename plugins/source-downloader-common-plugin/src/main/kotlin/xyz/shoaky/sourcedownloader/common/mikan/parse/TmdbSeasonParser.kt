package xyz.shoaky.sourcedownloader.common.mikan.parse

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import xyz.shoaky.sourcedownloader.common.mikan.MikanVariableProvider.Companion.log
import xyz.shoaky.sourcedownloader.external.tmdb.GetTvShow
import xyz.shoaky.sourcedownloader.external.tmdb.SearchTvShow
import xyz.shoaky.sourcedownloader.external.tmdb.TmdbClient

/**
 * 从奇葩的季度命名中获取季度
 */
// TODO 已经在主应用中的SeasonProvider实现，但是需要用某种方式把SubjectContent传递过去(暂无头绪)，完成后该类将移除
internal class TmdbSeasonParser(
    private val tmdbClient: TmdbClient
) : ValueParser {

    override val name: String = "TmdbParser"

    override fun apply(subjectContent: SubjectContent, filename: String): Result {
        val content = Content(subjectContent.subject.name)
        val season = tmdbCache.get(content)
        if (season < 1) {
            log.info("从TMDB获取季度失败name:${content.originName}")
            return Result()
        }
        log.debug("从TMDB获取季度成功name:${content.originName},season:$season")
        return Result(season, accuracy = Result.Accuracy.ACCURATE)
    }

    private val tmdbCache = CacheBuilder.newBuilder().maximumSize(500).build(
        object : CacheLoader<Content, Int>() {
            override fun load(content: Content): Int {
                val split = content.originName.split(" ")
                val search = split.first()
                val results = tmdbClient.execute(SearchTvShow(search)).body().results
                val firstSearchResult = results.firstOrNull() ?: return -1

                val tvShow = tmdbClient.execute(GetTvShow(firstSearchResult.id)).body()

                if (tvShow.numberOfSeasons == 1) {
                    return 1
                }

                val last = split.last()
                return tvShow.seasons.filter { it.name.contains(last) }
                    .map { it.seasonNumber }.firstOrNull() ?: -1
            }
        })
}

private data class Content(
    val originName: String,
)