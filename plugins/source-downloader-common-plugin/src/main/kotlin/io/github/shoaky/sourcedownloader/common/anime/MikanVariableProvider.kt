package io.github.shoaky.sourcedownloader.common.anime

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import io.github.shoaky.sourcedownloader.common.anime.parse.*
import io.github.shoaky.sourcedownloader.external.bangumi.BgmTvApiClient
import io.github.shoaky.sourcedownloader.external.bangumi.GetSubjectRequest
import io.github.shoaky.sourcedownloader.external.bangumi.Subject
import io.github.shoaky.sourcedownloader.external.tmdb.TmdbClient
import io.github.shoaky.sourcedownloader.sdk.*
import io.github.shoaky.sourcedownloader.sdk.component.VariableProvider
import org.slf4j.LoggerFactory
import java.net.URI

/**
 * Mikan变量提供器
 * 从[SourceItem.link]中爬取获取bgm.tv的subjectId
 */
class MikanVariableProvider(
    private val mikanToken: String? = null,
    private val mikanSupport: MikanSupport = MikanSupport(mikanToken),
    private val bgmTvClient: BgmTvApiClient = BgmTvApiClient(),
    tmdbClient: TmdbClient = TmdbClient(TmdbClient.DEFAULT_TOKEN)
) : VariableProvider {

    override val accuracy: Int = 3

    private val seasonParserChain = ParserChain(
        listOf(
            CommonSeasonParser,
            TmdbSeasonParser(tmdbClient),
            DefaultValueSeasonParser
        ), true
    )

    init {
        log.debug("Mikan初始化,token:{}", mikanToken)
    }

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
            val bangumiPageInfo = mikanSupport.getBangumiPageInfo(mikanBangumiHref)
            val subjectId = bangumiPageInfo.bgmTvSubjectId
                ?: throw RuntimeException("从$mikanBangumiHref 获取 BgmTv Subject失败")
            return bgmTvClient.execute(GetSubjectRequest(subjectId)).body()
        }.onFailure {
            log.error("获取Bangumi Subject失败 $mikanBangumiHref", it)
        }
        throw RuntimeException("获取Bangumi Subject失败")
    }

    override fun createSourceGroup(sourceItem: SourceItem): SourceItemGroup {
        // TODO cache
        val pageInfo = mikanSupport.getEpisodePageInfo(sourceItem.link)
        if (pageInfo.mikanHref == null) {
            log.warn("mikanHref is null, link:{}", sourceItem.link)
            return SourceItemGroup.EMPTY
        }

        val bangumiTitle = pageInfo.bangumiTitle!!
        val subject = bangumiCache.get(pageInfo.mikanHref)
        // 有些纯字母的没有中文名
        val searchContent = subject.nameCn.takeIf { it.isNotBlank() } ?: subject.name

        // 暂时没看到文件跨季度的情况
        val result = seasonParserChain.apply(SubjectContent(subject.name, subject.nameCn), sourceItem.title)

        val season = result.padValue() ?: "01"
        val bangumiInfo = BangumiInfo(
            subject.name,
            searchContent,
            bangumiTitle,
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