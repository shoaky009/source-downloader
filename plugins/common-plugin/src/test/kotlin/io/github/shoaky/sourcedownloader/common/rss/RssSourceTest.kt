package io.github.shoaky.sourcedownloader.common.rss

import io.github.shoaky.sourcedownloader.common.supplier.RssSourceSupplier
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Disabled
class RssSourceTest {

    private val rssSource = RssSourceSupplier.apply(
        CoreContext.empty,
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