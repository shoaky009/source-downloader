package io.github.shoaky.sourcedownloader.common.dlsite

import io.github.shoaky.sourcedownloader.common.supplier.DlsiteVariableProviderSupplier
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import org.junit.jupiter.api.Test
import java.net.URI
import java.nio.file.Files
import java.time.LocalDateTime
import kotlin.io.path.Path
import kotlin.test.assertEquals

class DlsiteVariableProviderTests {

    private val sourceItem = SourceItem(
        "RJ438225 xxxxxxxxxxxxxxxxdddaxxxxx",
        URI("https://www.dlsite.com/home/work/=/product_id/RJ01042626.html"),
        LocalDateTime.now(),
        "dlsite",
        URI("http://localhost")
    )

    private val provider = DlsiteVariableProviderSupplier.apply(Properties.EMPTY)

    @Test
    fun test_support() {
        assert(provider.support(sourceItem))
        assert(provider.support(sourceItem.copy("", URI("https://baidu.com"))).not())
    }

    @Test
    fun test_parse_work_info() {
        val body = Files.readString(Path("src", "test", "resources", "RJ01042626.html"))
        val workInfo = provider.parseWorkInfo(body, "RJ01042626")
        println(workInfo)
        assertEquals("RJ01042626", workInfo.dlsiteId)
        assertEquals(2023, workInfo.year)
        assertEquals("電撃G's magazine", workInfo.maker)
    }

    @Test
    fun test_group() {
        val group = provider.createSourceGroup(sourceItem).sharedPatternVariables() as DlsiteWorkInfo
        assertEquals("RJ01042626", group.dlsiteId)
    }

}