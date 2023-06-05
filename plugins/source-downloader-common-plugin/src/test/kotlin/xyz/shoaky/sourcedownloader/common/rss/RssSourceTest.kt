package xyz.shoaky.sourcedownloader.common.rss

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.common.supplier.RssSourceSupplier
import xyz.shoaky.sourcedownloader.sdk.Properties

class RssSourceTest {

    private val rssSource = RssSourceSupplier.apply(
        Properties.fromMap(
            mapOf("url" to "https://mikanani.me/RSS/Bangumi?bangumiId=2852&subgroupid=583")
        )
    )

    @Test
    fun normal() {
        val items = rssSource.fetch()
        assertEquals(12, items.toList().size)
    }
}