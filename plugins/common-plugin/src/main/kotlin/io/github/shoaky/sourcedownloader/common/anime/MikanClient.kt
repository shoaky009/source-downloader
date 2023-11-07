package io.github.shoaky.sourcedownloader.common.anime

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import org.jsoup.Jsoup
import java.net.URI
import java.net.URL

class MikanClient(
    private val token: String?
) {

    fun getBangumiPageInfo(url: URL): BangumiPageInfo {
        return bangumiPageCache.get(UrlKey(url, token))
    }

    fun getEpisodePageInfo(url: URL): EpisodePageInfo {
        return episodePageCache.get(UrlKey(url, token))
    }

    class EpisodePageInfo(
        val bangumiTitle: String? = null,
        val mikanHref: String? = null,
        val fansubRss: String? = null
    )

    class BangumiPageInfo(
        val bgmTvSubjectId: String? = null
    )

    companion object {

        private const val TOKEN_COOKIE = ".AspNetCore.Identity.Application"
        private val bangumiPageCache: LoadingCache<UrlKey, BangumiPageInfo> = CacheBuilder.newBuilder().maximumSize(500)
            .build(object : CacheLoader<UrlKey, BangumiPageInfo>() {
                override fun load(key: UrlKey): BangumiPageInfo {
                    return getBangumiPageInfo(key.url, key.token)
                }
            })

        private val episodePageCache: LoadingCache<UrlKey, EpisodePageInfo> = CacheBuilder.newBuilder().maximumSize(500)
            .build(object : CacheLoader<UrlKey, EpisodePageInfo>() {
                override fun load(key: UrlKey): EpisodePageInfo {
                    return getEpisodePageInfo(key.url, key.token)
                }
            })

        private fun getBangumiPageInfo(url: URL, token: String?): BangumiPageInfo {
            val page = Jsoup.newSession().cookie(TOKEN_COOKIE, token ?: "")
                .url(url).get().body()
            val subjectId = page.select(".bangumi-info a")
                .filter { ele ->
                    ele.hasText() && ele.text().contains("/subject/")
                }.map {
                    val text = it.text()
                    URI.create(text).pathSegments().last()
                }.first()
            return BangumiPageInfo(subjectId)
        }

        private fun getEpisodePageInfo(url: URL, token: String?): EpisodePageInfo {
            val connection = Jsoup.newSession().cookie(TOKEN_COOKIE, token ?: "")
            val body = connection.url(url).get().body()
            val titleElement = body.select(".bangumi-title a").first()
            val bangumiTitle = titleElement?.text()?.trim()

            val mikanHref = titleElement?.attr("abs:href")

            val fansubRss = body.select(".mikan-rss")
                .firstOrNull()?.attr("abs:href")

            return EpisodePageInfo(bangumiTitle, mikanHref, fansubRss)
        }
    }

    private class UrlKey(
        val url: URL,
        val token: String? = null
    ) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as UrlKey

            return url == other.url
        }

        override fun hashCode(): Int {
            return url.hashCode()
        }
    }
}