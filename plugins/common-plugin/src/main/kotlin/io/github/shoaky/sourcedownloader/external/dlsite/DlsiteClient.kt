package io.github.shoaky.sourcedownloader.external.dlsite

import io.github.shoaky.sourcedownloader.sdk.util.find
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DlsiteClient {

    fun getWorkInfo(dlsiteId: String, locale: String): DlsiteWorkInfo {
        val document = Jsoup.connect("https://www.dlsite.com/home/work/=/product_id/$dlsiteId.html")
            .cookie("locale", locale)
            .cookie("adultchecked", "1")
            .get()
        // val canonical = document.select("link[rel=canonical]").attr("href")
        // val dlsiteId = canonical.find(*idRegexes)
        //     ?: throw IllegalArgumentException("Can't find dlsiteId in document element 'link[rel=canonical]'")
        return parseWorkDetail(document, dlsiteId)
    }

    fun parseWorkDetail(document: Document, dlsiteId: String? = null): DlsiteWorkInfo {
        val id = dlsiteId
            ?: (document.select("link[rel=canonical]").attr("href").find(*idRegexes)
                ?: throw IllegalArgumentException("Can't find dlsiteId in document element 'link[rel=canonical]'"))

        val locale = document.tagName("lang") ?: ZH_CN
        val workOutline = document.getElementById("work_outline")?.select("tbody tr")
            ?.associateBy(
                { it.firstElementChild()?.text()?.trim() },
                { it.lastElementChild()?.text()?.trim() }
            )
            ?: return DlsiteWorkInfo(
                id,
                document.getElementById("work_name")?.ownText(),
            )

        val keys = localizationMapping[locale] ?: localizationMapping.getValue(ZH_CN)
        val releaseDate = kotlin.runCatching {
            LocalDate.parse(workOutline[keys[0]], chineseDateTimeFormatter)
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

    fun searchWork(keyword: String, locale: String): List<SearchWork> {
        val document = Jsoup
            .connect("https://www.dlsite.com/maniax/fsr/=/language/jp/keyword/$keyword")
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

    companion object {

        private val idRegexes = arrayOf(
            Regex("RJ\\d+"),
            Regex("VJ\\d+"),
        )
        private val chineseDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")
        private val localizationMapping: Map<String, List<String>> = mapOf(
            "zh-cn" to listOf(
                "贩卖日", "系列名", "剧情", "插画", "声优", "音乐", "作品类型", "作者", "社团名"
            ),
            "ja-jp" to listOf(
                "販売日", "シリーズ名", "シナリオ", "イラスト", "声優", "音楽", "作品形式", "作者", "サークル名"
            ),
            // TODO more language
        )
        private const val ZH_CN = "zh-cn"
        fun parseDlsiteId(text: String): String? {
            return text.find(*idRegexes)
        }
    }
}