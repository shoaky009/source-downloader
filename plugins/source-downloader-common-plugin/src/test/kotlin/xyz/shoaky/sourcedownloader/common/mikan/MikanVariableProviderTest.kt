package xyz.shoaky.sourcedownloader.common.mikan

import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.sdk.SourceItem
import java.net.URI
import java.time.LocalDateTime
import kotlin.io.path.Path
import kotlin.test.assertEquals

class MikanVariableProviderTest {

    private val mikanVariableProvider = MikanVariableProvider()

    @Test
    fun normal() {
        val sourceItem = SourceItem(
            "[ANi] 前進吧！登山少女  Next Summit（僅限港澳台地區） - 11 [1080P][Bilibili][WEB-DL][AAC AVC][CHT CHS]",
            URI("https://mikanani.me/Home/Episode/324b70bd8a170bcf13d7f5bdf9d3e8df4065f682"),
            LocalDateTime.now(), "application/x-bittorrent",
            URI("https://mikanani.me/Download/20221214/324b70bd8a170bcf13d7f5bdf9d3e8df4065f682.torrent")
        )
        val sourceGroup = mikanVariableProvider.createSourceGroup(sourceItem)
        val sourceFiles = sourceGroup.sourceFiles(listOf(Path("[ANi] 前進吧！登山少女  Next Summit（僅限港澳台地區） - 11 [1080P][Bilibili][WEB-DL][AAC AVC][CHT CHS].mp4")))
        assertEquals(1, sourceFiles.size)

        val bangumiInfo = sourceGroup.sharedPatternVariables() as BangumiInfo
        assertEquals(bangumiInfo.season, "01")
        val sourceFile = sourceGroup.sourceFiles(listOf(Path("[ANi] 前進吧！登山少女  Next Summit（僅限港澳台地區） - 11 [1080P][Bilibili][WEB-DL][AAC AVC][CHT CHS].mp4"))).first()
        assertEquals(sourceFile.patternVariables().variables()["episode"], "11")

    }
}