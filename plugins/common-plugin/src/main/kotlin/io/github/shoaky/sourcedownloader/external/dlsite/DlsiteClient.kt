package io.github.shoaky.sourcedownloader.external.dlsite

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.google.common.net.HttpHeaders
import io.github.shoaky.sourcedownloader.sdk.util.find
import io.github.shoaky.sourcedownloader.sdk.util.http.CommonBodyHandler
import io.github.shoaky.sourcedownloader.sdk.util.http.httpClient
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpRequest
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DlsiteClient(
    private val userAgent: String = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36"
) {

    fun getWorkInfo(dlsiteId: String, locale: String): DlsiteWorkInfo {
        val document = Jsoup.connect("https://www.dlsite.com/home/work/=/product_id/$dlsiteId.html")
            .cookie("locale", locale)
            .cookie("adultchecked", "1")
            .get()
        return parseWorkDetail(document, dlsiteId)
    }

    fun parseWorkDetail(document: Document, dlsiteId: String? = null): DlsiteWorkInfo {
        val id = dlsiteId
            ?: (document.select("link[rel=canonical]").attr("href").find(*idRegexes)
                ?: throw IllegalArgumentException("Can't find dlsiteId in document element 'link[rel=canonical]'"))

        val locale = document.root().children().attr("lang").takeIf { it.isNotEmpty() } ?: JA_JP
        val workOutline = document.getElementById("work_outline")?.select("tbody tr")
            ?.associateBy(
                { it.firstElementChild()?.text()?.trim() },
                { it.lastElementChild()?.text()?.trim() }
            )
            ?: return DlsiteWorkInfo(
                id,
                document.getElementById("work_name")?.ownText(),
            )

        val keys = localizationMapping[locale] ?: localizationMapping.getValue(JA_JP)
        val releaseDate = kotlin.runCatching {
            workOutline[keys[0]]?.let { LocalDate.parse(it, chineseDateTimeFormatter) }
        }.getOrNull()

        return DlsiteWorkInfo(
            id,
            document.getElementById("work_name")?.ownText(),
            releaseDate?.toString(),
            releaseDate?.year,
            releaseDate?.monthValue,
            releaseDate?.dayOfMonth,
            workOutline[keys[1]],
            productFormat = workOutline[keys[6]],
            author = workOutline[keys[7]],
            maker = document.select("#work_maker .maker_name").text().trim()
        )
    }

    fun searchWork(
        keyword: String,
        locale: String = "ja-jp",
        workTypeCategories: List<String> = emptyList()
    ): List<SearchWork> {
        var url =
            "https://www.dlsite.com/maniax/fsr/=/language/jp/keyword/${URLEncoder.encode(keyword, Charsets.UTF_8)}"
        if (workTypeCategories.isNotEmpty()) {
            val conditions = workTypeCategories.mapIndexed { idx, category ->
                val encoded = URLEncoder.encode("work_type_category[$idx]", Charsets.UTF_8)
                "$encoded/$category"
            }.joinToString("/", "/")
            url += conditions
        }

        val document = Jsoup
            .connect(url)
            .cookie("locale", locale)
            .cookie("adultchecked", "1")
            .get()

        return document.select("#search_result_img_box .search_result_img_box_inner").mapNotNull {
            val ele = it.select(".work_name a")
            val id = ele.attr("href").find(*idRegexes) ?: return@mapNotNull null
            val title = ele.attr("title")
            SearchWork(id, title)
        }
    }

    fun suggestWork(keyword: String, locale: String = "ja-jp"): SuggestResponse {
        val encoded = URLEncoder.encode(keyword, Charsets.UTF_8)
        val url = "https://www.dlsite.com/suggest/?term=$encoded&site=pro&time=${System.currentTimeMillis()}"
        val request = HttpRequest.newBuilder(URI.create(url)).GET()
            .header(HttpHeaders.USER_AGENT, userAgent)
            .header("Cookie", "locale=$locale; adultchecked=1")
            .build()
        val bodyHandler = CommonBodyHandler(jacksonTypeRef<SuggestResponse>())
        return httpClient.send(request, bodyHandler).body()
    }

    companion object {

        private val idRegexes = arrayOf(
            Regex("RJ\\d+"),
            Regex("VJ\\d+"),
        )
        private val chineseDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")
        private const val ZH_CN = "zh-cn"
        private const val JA_JP = "ja-jp"
        private val localizationMapping: Map<String, List<String>> = mapOf(
            ZH_CN to listOf(
                "贩卖日", "系列名", "剧情", "插画", "声优", "音乐", "作品类型", "作者", "社团名"
            ),
            JA_JP to listOf(
                "販売日", "シリーズ名", "シナリオ", "イラスト", "声優", "音楽", "作品形式", "作者", "サークル名"
            ),
        )

        fun parseDlsiteId(text: String): String? {
            return text.find(*idRegexes)
        }
    }
}

data class SuggestResponse(
    val work: List<SuggestWork>
)

data class SuggestWork(
    @param:JsonProperty("work_name")
    val workName: String,
    @param:JsonProperty("workno")
    val workNo: String,
    @param:JsonProperty("maker_name")
    val markerName: String? = null,
    @param:JsonProperty("work_type")
    val workType: String? = null
)