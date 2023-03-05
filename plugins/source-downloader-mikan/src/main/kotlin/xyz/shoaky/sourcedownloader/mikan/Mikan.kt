package xyz.shoaky.sourcedownloader.mikan

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.springframework.web.util.UriComponentsBuilder
import xyz.shoaky.sourcedownloader.mikan.parse.ParserChain
import xyz.shoaky.sourcedownloader.mikan.parse.SubjectContent
import xyz.shoaky.sourcedownloader.sdk.*
import xyz.shoaky.sourcedownloader.sdk.api.bangumi.BangumiApiClient
import xyz.shoaky.sourcedownloader.sdk.api.bangumi.GetSubjectRequest
import xyz.shoaky.sourcedownloader.sdk.api.bangumi.Subject
import xyz.shoaky.sourcedownloader.sdk.component.*
import java.nio.file.Path

class Mikan : SourceContentCreator {

    companion object {
        internal val log = LoggerFactory.getLogger(Mikan::class.java)
        internal val bangumiCache =
            CacheBuilder.newBuilder().maximumSize(500).build(object : CacheLoader<String, Subject>() {
                override fun load(key: String): Subject {
                    return getBangumiSubject(key)
                }
            })

        private fun getBangumiSubject(mikanBangumiHref: String): Subject {
            val page = Jsoup.newSession().url(mikanBangumiHref).get().body()
            val subjectId = page.select(".bangumi-info a").filter { ele ->
                ele.hasText() && ele.text().contains("/subject/")
            }.map {
                val text = it.text()
                val build = UriComponentsBuilder.fromHttpUrl(text).build()
                build.pathSegments.last()
            }.first()
            return BangumiApiClient.execute(GetSubjectRequest(subjectId)).body()
        }
    }

    override fun createSourceGroup(sourceItem: SourceItem): SourceGroup {
        val body = Jsoup.newSession().url(sourceItem.link).get().body()
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
        val mainPatternVars = PatternVars()

        mainPatternVars.addVar("mikan-title", mikanTitle)
        mainPatternVars.addVar("name", subject.name)
        mainPatternVars.addVar("name-cn", subject.nameCn)
        mainPatternVars.addVar("date", subject.date.toString())
        mainPatternVars.addVar("year", subject.date.year)
        mainPatternVars.addVar("month", subject.date.monthValue)

        val mainName = SubjectContent(subject, mikanTitle)
        //暂时没看到文件跨季度的情况
        val parserChain = ParserChain.seasonChain()
        val result = parserChain.apply(mainName, sourceItem.title)

        val season = result.value?.toString() ?: "01"
        mainPatternVars.addVar("season", season)
        return MikanSourceGroup(sourceItem, mainPatternVars, mainName)
    }

    override fun defaultSavePathPattern(): PathPattern {
        return PathPattern("{name}/Season {season}/")
    }

    override fun defaultFilenamePattern(): PathPattern {
        return PathPattern("{name} - S{season}E{episode}")
    }

}

private class MikanSourceGroup(
    private val sourceItem: SourceItem,
    private val mainPatternVars: PatternVars,
    private val subject: SubjectContent,
) : SourceGroup {

    override fun sourceFiles(paths: List<Path>): List<SourceFile> {
        return paths.map { path ->
            BangumiFile(path, mainPatternVars.copy(), subject)
        }
    }

    override fun createDownloadTask(downloadPath: Path, options: DownloadOptions): DownloadTask {
        //在downloader中做
        val link = sourceItem.link.toString()
        val torrentHash = link.substring(link.lastIndexOf('/') + 1)
        return DownloadTask.createTorrentTask(
            sourceItem, torrentHash, downloadPath, options.category ?: "Bangumi"
        )
    }
}

object MikanCreatorSupplier : SdComponentSupplier<Mikan> {
    override fun apply(props: ComponentProps): Mikan {
        return Mikan()
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(ComponentType.creator("mikan"))
    }

    override fun rules(): List<ComponentRule> {
        return listOf(ComponentRule.allowDownloader(TorrentDownloader::class))
    }
}
