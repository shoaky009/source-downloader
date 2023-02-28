package xyz.shoaky.sourcedownloader.core.component

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RssSourceTest {
    @Test
    fun normal() {
        val rssSource = RssSource("https://mikanani.me/RSS/Bangumi?bangumiId=2852&subgroupid=583")
        val items = rssSource.fetch()
        assertEquals(12, items.size)
    }
}