package io.github.shoaky.sourcedownloader.common.anime

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import io.github.shoaky.sourcedownloader.external.bangumi.BgmTvApiClient
import io.github.shoaky.sourcedownloader.external.bangumi.GetSubjectRequest
import io.github.shoaky.sourcedownloader.external.bangumi.Subject
import io.github.shoaky.sourcedownloader.external.season.*
import io.github.shoaky.sourcedownloader.external.tmdb.TmdbClient
import io.github.shoaky.sourcedownloader.sdk.*
import io.github.shoaky.sourcedownloader.sdk.component.ComponentException
import io.github.shoaky.sourcedownloader.sdk.component.VariableProvider
import org.slf4j.LoggerFactory
import java.net.URI

/**
 * Mikan变量提供器
 * 从[SourceItem.link]中爬取获取bgm.tv的subjectId
 */
class MikanVariableProvider(
    private val mikanClient: MikanClient = MikanClient(null),
    private val bgmTvClient: BgmTvApiClient = BgmTvApiClient(),
    tmdbClient: TmdbClient = TmdbClient(TmdbClient.DEFAULT_TOKEN)
) : VariableProvider {

    override val accuracy: Int = 3

    private val seasonSupport = SeasonSupport(
        listOf(
            SpSeasonParser,
            GeneralSeasonParser,
            LastStringSeasonParser,
            TmdbSeasonParser(tmdbClient)
        ), withDefault = true
    )

    companion object {

        internal val log = LoggerFactory.getLogger(MikanVariableProvider::class.java)
    }

    private val bangumiCache =
        CacheBuilder.newBuilder().maximumSize(500).build(object : CacheLoader<String, Subject>() {
            override fun load(key: String): Subject {
                return getBangumiSubject(key)
            }
        })

    private fun getBangumiSubject(mikanBangumiHref: String): Subject {
        kotlin.runCatching {
            val bangumiPageInfo = mikanClient.getBangumiPageInfo(
                URI(mikanBangumiHref).toURL()
            )
            val subjectId = bangumiPageInfo.bgmTvSubjectId
                ?: throw ComponentException.processing("从$mikanBangumiHref 获取 BgmTv Subject失败")
            return bgmTvClient.execute(GetSubjectRequest(subjectId)).body()
        }.onFailure {
            log.error("获取Bangumi Subject失败 $mikanBangumiHref", it)
        }
        throw ComponentException.processing("获取Bangumi Subject失败")
    }

    override fun createSourceGroup(sourceItem: SourceItem): SourceItemGroup {
        val pageInfo = mikanClient.getEpisodePageInfo(sourceItem.link.toURL())
        if (pageInfo.mikanHref == null) {
            log.warn("mikanHref is null, link:{}", sourceItem.link)
            return SourceItemGroup.EMPTY
        }

        val subject = try {
            bangumiCache.get(pageInfo.mikanHref)
        } catch (e: Exception) {
            log.warn("获取Bangumi Subject失败, item:{}", sourceItem, e)
            return SourceItemGroup.EMPTY
        }

        // 有些纯字母的没有中文名
        val nameCn = subject.nameCn.takeIf { it.isNotBlank() } ?: subject.name
        val season = seasonSupport.padValue(
            ParseValue(sourceItem.title, listOf(0, 1)),
            ParseValue(subject.name),
            ParseValue(nameCn),
        )

        val bangumiInfo = BangumiInfo(
            subject.name,
            nameCn,
            pageInfo.bangumiTitle,
            subject.date.toString(),
            subject.date.year,
            subject.date.monthValue,
            season
        )
        return MikanSourceGroup(bangumiInfo)
    }

    override fun support(item: SourceItem): Boolean {
        return item.link.host.contains("mikan")
    }

}

private class MikanSourceGroup(
    private val bangumiInfo: BangumiInfo,
) : SourceItemGroup {

    override fun filePatternVariables(paths: List<SourceFile>): List<FileVariable> {
        return paths.map {
            BangumiFile(bangumiInfo)
        }
    }

    override fun sharedPatternVariables(): PatternVariables {
        return bangumiInfo
    }
}

fun URI.pathSegments(): List<String> {
    return path.split("/").filter { it.isNotBlank() }
}

data class BangumiInfo(
    val name: String? = null,
    val nameCn: String? = null,
    val mikanTitle: String? = null,
    val date: String? = null,
    val year: Int? = null,
    val month: Int? = null,
    val season: String? = null,
) : PatternVariables