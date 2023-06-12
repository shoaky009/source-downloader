package io.github.shoaky.sourcedownloader.common.dlsite

import io.github.shoaky.sourcedownloader.sdk.FunctionalItemGroup
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.SourceItemGroup
import io.github.shoaky.sourcedownloader.sdk.UniversalFileVariable
import io.github.shoaky.sourcedownloader.sdk.component.VariableProvider
import io.github.shoaky.sourcedownloader.sdk.util.find
import io.github.shoaky.sourcedownloader.sdk.util.http.httpClient
import io.github.shoaky.sourcedownloader.sdk.util.http.httpGetRequest
import org.jsoup.Jsoup
import java.net.URI
import java.net.http.HttpResponse
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal class DlsiteVariableProvider(
    private val locale: String = "zh-cn"
) : VariableProvider {
    override fun createSourceGroup(sourceItem: SourceItem): SourceItemGroup {
        val dlsiteId = getDlsiteId(sourceItem) ?: throw IllegalArgumentException("not dlsite item")

        val workInfo = getWorkInfo(dlsiteId)
        return FunctionalItemGroup(
            workInfo,
        ) { UniversalFileVariable(workInfo) }
    }

    private fun getWorkInfo(
        dlsiteId: String
    ): DlsiteWorkInfo {
        // 不用jsoup来请求是因为后续要对http请求统一管理,增加功能
        //TODO cache
        val request = httpGetRequest(
            URI("https://www.dlsite.com/home/work/=/product_id/$dlsiteId.html"),
            mapOf("Cookie" to "locale=$locale; adultchecked=1")
        )
        val body = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body()
        return parseWorkInfo(body, dlsiteId)
    }

    fun parseWorkInfo(
        body: String,
        dlsiteId: String
    ): DlsiteWorkInfo {
        val document = Jsoup.parse(body)
        val workOutline = document.getElementById("work_outline")?.select("tbody tr")
            ?.associateBy(
                { it.firstElementChild()?.text()?.trim() },
                { it.lastElementChild()?.text()?.trim() }
            )
            ?: return DlsiteWorkInfo(
                dlsiteId,
                document.getElementById("work_name")?.ownText(),
            )


        val keys = map[locale] ?: map["zh-cn"]!!

        val releaseDate = kotlin.runCatching {
            LocalDate.parse(workOutline[keys[0]], chineseDateTimeFormatter)
        }.getOrNull()
        // 感觉命名用不上 暂定
        // val scenario = workOutline[keys[2]]?.split("/")?.map { it.trim() }
        // val illustration = workOutline[keys[3]]?.split("/")?.map { it.trim() }
        // val voiceActor = workOutline[keys[4]]?.split("/")?.map { it.trim() }

        return DlsiteWorkInfo(
            dlsiteId,
            document.getElementById("work_name")?.ownText(),
            releaseDate?.year,
            releaseDate?.monthValue,
            releaseDate?.dayOfMonth,
            workOutline[keys[1]],
            productFormat = workOutline[keys[6]],
            author = workOutline[keys[7]],
            maker = document.select("#work_maker .maker_name").text().trim()
        )
    }

    override fun support(item: SourceItem): Boolean {
        return getDlsiteId(item) != null
    }

    companion object {

        private val idRegexes = arrayOf(
            Regex("RJ\\d+"),
            Regex("VJ\\d+"),
        )
        private val chineseDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")

        private fun getDlsiteId(item: SourceItem): String? {
            val link = item.link.toString()
            return link.find(*idRegexes)
        }

        private val map: Map<String, List<String>> = mapOf(
            "zh-cn" to listOf(
                "贩卖日", "系列名", "剧情", "插画", "声优", "音乐", "作品类型", "作者", "社团名"
            ),
            "ja-jp" to listOf(
                "販売日", "シリーズ名", "シナリオ", "イラスト", "声優", "音楽", "作品形式", "作者", "サークル名"
            ),

            // TODO more language
        )
    }
}


