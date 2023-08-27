package io.github.shoaky.sourcedownloader.common.anime.parse

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import io.github.shoaky.sourcedownloader.external.tmdb.GetTvShow
import io.github.shoaky.sourcedownloader.external.tmdb.SearchTvShow
import io.github.shoaky.sourcedownloader.external.tmdb.TmdbClient
import org.slf4j.LoggerFactory
import java.nio.file.Path

/**
 * 从奇葩的季度命名中获取季度
 */
// TODO 已经在主应用中的SeasonProvider实现，但是需要用某种方式把SubjectContent传递过去(暂无头绪)，完成后该类将移除
internal class TmdbSeasonParser(
    private val tmdbClient: TmdbClient
) : ValueParser {

    override val name: String = "TmdbParser"

    override fun apply(content: SubjectContent, file: Path): Result {
        val season = tmdbCache.get(content)
        if (season < 1) {
            log.info("从TMDB获取季度失败name:${content}")
            return Result()
        }
        log.debug("从TMDB获取季度成功name:{},season:{}", content, season)
        return Result(season, accuracy = Result.Accuracy.ACCURATE)
    }

    private val tmdbCache = CacheBuilder.newBuilder().maximumSize(500).build(
        object : CacheLoader<SubjectContent, Int>() {
            override fun load(content: SubjectContent): Int {
                val split = content.originName.split(" ")
                val search = split.first()
                val results = tmdbClient.execute(SearchTvShow(search)).body().results
                val firstSearchResult = results.firstOrNull() ?: return -1

                val tvShow = tmdbClient.execute(GetTvShow(firstSearchResult.id)).body()

                if (tvShow.numberOfSeasons == 1) {
                    return 1
                }

                val last = split.last()
                return tvShow.seasons.map { it.copy(name = it.name.replace(" ", "")) }
                    .filter { it.name.contains(last) }
                    .map { it.seasonNumber }.firstOrNull() ?: -1
            }
        })

    companion object {

        private val log = LoggerFactory.getLogger(TmdbSeasonParser::class.java)
    }
}