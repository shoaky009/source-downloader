package io.github.shoaky.sourcedownloader.common.getchu

import io.github.shoaky.sourcedownloader.sdk.util.queryMap
import org.jsoup.Jsoup
import java.net.URI
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object GetchuClient {

    private val releaseDateFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd")

    fun searchKeyword(title: String): List<GetchuSearchItem> {
        val doc = Jsoup.connect("https://www.getchu.com/php/search.phtml?search_keyword=$title")
            .cookie("getchu_adalt_flag", "getchu.com")
            .get()
        return doc.select(".search_container .display li").map {
            val blueb = it.select("#detail_block .blueb")
            GetchuSearchItem(it, blueb.text(), blueb.attr("abs:href"))
        }
    }

    fun itemDetail(url: String): GetchuDetailItem? {
        val getchuId = URI(url).queryMap()["id"] ?: return null

        val doc = Jsoup.connect(url)
            .cookie("getchu_adalt_flag", "getchu.com")
            .get()
        val brand = doc.getElementById("brandsite")?.ownText()
        val title = doc.getElementById("soft-title")?.ownText()
        val p = doc.select("#soft_table tbody tr:nth-child(2) tr td:nth-child(1)")
        val targets = mapOf("発売日：" to "releaseDate", "品番：" to "isbn")
        val fieldContent = p.mapNotNull {
            val elementFieldName = it.text()
            val itemFieldName = targets[elementFieldName] ?: return@mapNotNull null
            val next = it.nextElementSibling()
            val nNext = next?.nextElementSibling()?.text()
            val content = nNext ?: next?.text() ?: return@mapNotNull null
            itemFieldName to content
        }.toMap()

        val isbn = fieldContent["isbn"]
        val releaseDate = fieldContent["releaseDate"]?.let {
            LocalDate.parse(it, releaseDateFormat)
        }
        return GetchuDetailItem(getchuId, title, isbn, brand, releaseDate)
    }


}