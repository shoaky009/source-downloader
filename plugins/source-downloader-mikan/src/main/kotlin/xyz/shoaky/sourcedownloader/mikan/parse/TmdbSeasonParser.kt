package xyz.shoaky.sourcedownloader.mikan.parse

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import xyz.shoaky.sourcedownloader.mikan.Mikan.Companion.log
import xyz.shoaky.sourcedownloader.sdk.util.Http
import java.net.ProxySelector
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest

/**
 * 从奇葩的季度命名中获取季度
 */
internal class TmdbSeasonParser(private val apiKey: String) : ValueParser {

    override val name: String = "TmdbParser"


    override fun apply(subjectContent: SubjectContent, filename: String): Result {
        val nameCn = subjectContent.subject.nameCn
        val season = tmdbCache.get(Content(nameCn, apiKey))
        if (season < 1) {
            log.info("从TMDB获取季度失败name:$nameCn")
            return Result()
        }
        return Result(season, accuracy = Result.Accuracy.ACCURATE)
    }

    private data class Content(val nameCn: String, val apiKey: String)

    private data class TmdbResult(
        val results: List<PageResult>
    )

    private data class PageResult(
        val id: Long,
        val name: String,
        @JsonProperty("original_name")
        val originalName: String
    )

    private data class TvShow(
        @JsonProperty("number_of_seasons")
        val numberOfSeasons: Int? = null,
        val seasons: List<Season>
    )

    private data class Season(
        val name: String,
        @JsonProperty("season_number")
        val seasonNumber: Int,
    )

    companion object {
        private val tmdbCache =
            CacheBuilder.newBuilder().maximumSize(500).build(object : CacheLoader<Content, Int>() {
                override fun load(content: Content): Int {
                    val nameCn = content.nameCn
                    val apiKey = content.apiKey
                    val tvId = getTvShowId(nameCn, apiKey) ?: return -1
                    val tvShow = getTvShow(tvId, apiKey) ?: return -1
                    return tvShow.seasons.filter { it.name == nameCn }.map { it.seasonNumber }.firstOrNull() ?: -1
                }
            })

        private fun getTvShow(tvId: Long, apiKey: String): TvShow? {
            val request = HttpRequest.newBuilder()
                .GET()
                .uri(URI("https://api.themoviedb.org/3/tv/$tvId?api_key=${apiKey}&language=zh-CN"))
                .build()
            val response = newHttpClient.send(request, Http.JsonBodyHandler(object : TypeReference<TvShow>() {}))
            if (response.statusCode() != 200) {
                log.error("获取TVShow失败,code:${response.statusCode()} body:${response.body()} request:$request")
                return null
            }
            return response.body()
        }

        private fun getTvShowId(nameCn: String, apiKey: String): Long? {
            val encode = URLEncoder.encode(nameCn, Charsets.UTF_8)
            val uri =
                URI("https://api.themoviedb.org/3/search/tv?api_key=$apiKey&language=zh-CN&page=1&query=$encode&include_adult=true")
            val request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build()
            val body = newHttpClient.send(request, Http.JsonBodyHandler(object : TypeReference<TmdbResult>() {})).body()
            return body.results.filter { nameCn.contains(it.name) }
                .map { it.id }.firstOrNull()
        }


        private val newHttpClient = createHttpClient()

        private fun createHttpClient(): HttpClient {
            val proxy = ProxySelector.getDefault()
            return HttpClient.newBuilder().proxy(proxy).build()
        }


    }

}