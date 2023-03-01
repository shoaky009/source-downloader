package xyz.shoaky.sourcedownloader.mikan

import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.sdk.SourceItem
import java.net.URL
import kotlin.io.path.Path
import kotlin.test.assertEquals

class MikanTest {

    private val mikan = Mikan()

    @Test
    fun normal() {
        val sourceItem = SourceItem(
            "[ANi] 前進吧！登山少女  Next Summit（僅限港澳台地區） - 11 [1080P][Bilibili][WEB-DL][AAC AVC][CHT CHS]",
            URL("https://mikanani.me/Home/Episode/324b70bd8a170bcf13d7f5bdf9d3e8df4065f682"),
            "application/x-bittorrent",
            URL("https://mikanani.me/Download/20221214/324b70bd8a170bcf13d7f5bdf9d3e8df4065f682.torrent")
        )
        val sourceGroup = mikan.createSourceGroup(sourceItem)
        val sourceFiles = sourceGroup.sourceFiles(listOf(Path("[ANi] 前進吧！登山少女  Next Summit（僅限港澳台地區） - 11 [1080P][Bilibili][WEB-DL][AAC AVC][CHT CHS].mp4")))
        assertEquals(1, sourceFiles.size)
        val sourceFile = sourceGroup.sourceFiles(listOf(Path("[ANi] 前進吧！登山少女  Next Summit（僅限港澳台地區） - 11 [1080P][Bilibili][WEB-DL][AAC AVC][CHT CHS].mp4"))).first()

        val downloadPath = Path("/downloads")
        val downloadSavePath = sourceFile.downloadSavePath(downloadPath)
        assertEquals(downloadPath.resolve("${sourceItem.title}.mp4"), downloadSavePath)

        val patternVars = sourceFile.patternVars()
        assertEquals(patternVars.getVar("episode"), "11")
        assertEquals(patternVars.getVar("season"), "1")
    }
}