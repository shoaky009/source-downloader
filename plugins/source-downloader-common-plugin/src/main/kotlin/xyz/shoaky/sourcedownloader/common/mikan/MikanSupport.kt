package xyz.shoaky.sourcedownloader.common.mikan

import org.jsoup.Jsoup
import java.net.URI

class MikanSupport(
    private val token: String?
) {

    fun getBangumiPageInfo(url: String): BangumiPageInfo {
        // TODO 校验url如果不是bangumi的页面则直接返回
        val page = Jsoup.newSession().cookie(".AspNetCore.Identity.Application", token ?: "")
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

    fun getEpisodePageInfo(uri: URI): EpisodePageInfo {
        val connection = Jsoup.newSession().cookie(".AspNetCore.Identity.Application", token ?: "")
        val body = connection.url(uri.toURL()).get().body()
        val titleElement = body.select(".bangumi-title a").first()
        val bangumiTitle = titleElement?.text()?.trim()

        val mikanHref = titleElement?.attr("abs:href")

        val fansubRss = body.select(".mikan-rss")
            .firstOrNull()?.attr("abs:href")

        return EpisodePageInfo(bangumiTitle, mikanHref, fansubRss)
    }

    class EpisodePageInfo internal constructor(
        val bangumiTitle: String? = null,
        val mikanHref: String? = null,
        val fansubRss: String? = null
    )

    class BangumiPageInfo internal constructor(
        val bgmTvSubjectId: String? = null
    )
}