package xyz.shoaky.sourcedownloader.mikan

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import xyz.shoaky.sourcedownloader.mikan.parse.ParserChain
import xyz.shoaky.sourcedownloader.mikan.parse.SubjectContent
import xyz.shoaky.sourcedownloader.sdk.PatternVariables
import xyz.shoaky.sourcedownloader.sdk.SourceFile
import xyz.shoaky.sourcedownloader.sdk.SourceItem
import xyz.shoaky.sourcedownloader.sdk.SourceItemGroup
import xyz.shoaky.sourcedownloader.sdk.api.bangumi.BangumiApiClient
import xyz.shoaky.sourcedownloader.sdk.api.bangumi.GetSubjectRequest
import xyz.shoaky.sourcedownloader.sdk.api.bangumi.Subject
import xyz.shoaky.sourcedownloader.sdk.component.*
import java.net.URI
import java.nio.file.Path

class MikanVariableProvider(
    private val mikanToken: String? = null
) : VariableProvider {

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
            val page = Jsoup.newSession().cookie(".AspNetCore.Identity.Application", mikanToken ?: "")
                .url(mikanBangumiHref).get().body()
            val subjectId = page.select(".bangumi-info a")
                .filter { ele ->
                    ele.hasText() && ele.text().contains("/subject/")
                }.map {
                    val text = it.text()
                    URI.create(text).pathSegments().last()
                }.first()
            return BangumiApiClient.execute(GetSubjectRequest(subjectId)).body()
        }.onFailure {
            log.error("获取Bangumi Subject失败 $mikanBangumiHref", it)
        }
        throw RuntimeException("获取Bangumi Subject失败")
    }

    override fun createSourceGroup(sourceItem: SourceItem): SourceItemGroup {
        val connection = Jsoup.newSession().cookie(".AspNetCore.Identity.Application", mikanToken ?: "")
        val body = connection.url(sourceItem.link.toURL()).get().body()
        val titleElement = body.select(".bangumi-title a").first()
        val mikanTitle = titleElement?.text()?.trim()
        if (mikanTitle == null) {
            log.error("mikanTitle is null,sourceItem:{}", sourceItem)
            throw RuntimeException("mikanTitle not found")
        }
        val mikanHref = titleElement.attr("href").let {
            if (it.startsWith("https://mikanani.me").not()) {
                "https://mikanani.me$it"
            } else {
                it
            }
        }
        val subject = bangumiCache.get(mikanHref)

        val subjectContent = SubjectContent(subject, mikanTitle)

        // 暂时没看到文件跨季度的情况
        val parserChain = ParserChain.seasonChain()
        val result = parserChain.apply(subjectContent, sourceItem.title)

        val season = result.padValue() ?: "01"
        val bangumiInfo = BangumiInfo(
            subject.name,
            // 有些纯字母的没有中文名
            subjectContent.nonEmptyName(),
            mikanTitle,
            subject.date.toString(),
            subject.date.year,
            subject.date.monthValue,
            season
        )
        return MikanSourceGroup(bangumiInfo, subjectContent)
    }

    override fun support(item: SourceItem): Boolean {
        return item.link.host == "mikanani.me"
    }

}

private class MikanSourceGroup(
    private val bangumiInfo: BangumiInfo,
    private val subject: SubjectContent,
) : SourceItemGroup {

    override fun sourceFiles(paths: List<Path>): List<SourceFile> {
        return paths.map { path ->
            BangumiFile(path, subject)
        }
    }

    override fun sharedPatternVariables(): PatternVariables {
        return bangumiInfo
    }
}

object MikanVariableProviderSupplier : SdComponentSupplier<MikanVariableProvider> {
    override fun apply(props: ComponentProps): MikanVariableProvider {
        val token = props.properties["token"]?.toString()
        return MikanVariableProvider(token)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(ComponentType.provider("mikan"))
    }

    override fun rules(): List<ComponentRule> {
        return listOf(ComponentRule.allowDownloader(TorrentDownloader::class))
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