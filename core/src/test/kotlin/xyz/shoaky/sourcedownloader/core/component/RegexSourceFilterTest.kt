package xyz.shoaky.sourcedownloader.core.component

import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.sdk.SourceItem
import java.net.URL

class RegexSourceFilterTest {

    private val sourceItem = SourceItem("dsadas test", URL("http://localhost"), "", URL("http://localhost"))

    @Test
    fun normal() {
        val filter = RegexSourceFilter(listOf(Regex("test"), Regex("a.c")))
        assert(!filter.test(sourceItem))
        assert(!filter.test(sourceItem.copy("abc")))
        assert(filter.test(sourceItem.copy("12346,!.")))
    }
}