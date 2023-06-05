package xyz.shoaky.sourcedownloader.common.mikan

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import xyz.shoaky.sourcedownloader.common.anime.BangumiInfo
import xyz.shoaky.sourcedownloader.common.anime.MikanSupport
import xyz.shoaky.sourcedownloader.common.anime.MikanVariableProvider
import xyz.shoaky.sourcedownloader.common.torrent.R
import xyz.shoaky.sourcedownloader.external.bangumi.BangumiRequest
import xyz.shoaky.sourcedownloader.external.bangumi.BgmTvApiClient
import xyz.shoaky.sourcedownloader.external.bangumi.Subject
import xyz.shoaky.sourcedownloader.sdk.SourceFile
import xyz.shoaky.sourcedownloader.sdk.SourceItem
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.io.path.Path
import kotlin.test.assertEquals

class MikanVariableProviderTest {

    @Test
    fun normal() {
        val mikanSupport = Mockito.mock(MikanSupport::class.java)
        Mockito.`when`(mikanSupport.getEpisodePageInfo(
            URI("https://mikanani.me/Home/Episode/324b70bd8a170bcf13d7f5bdf9d3e8df4065f682")
        ))
            .thenReturn(MikanSupport.EpisodePageInfo(
                "向山进发 Next Summit",
                "https://mikanani.me/Home/Bangumi/2852#583",
                "https://mikanani.me/RSS/Bangumi?bangumiId=2852&subgroupid=583"
            ))

        Mockito.`when`(mikanSupport.getBangumiPageInfo(
            Mockito.anyString()
        )).thenReturn(MikanSupport.BangumiPageInfo("290980"))

        val bgmTvApiClient = Mockito.mock(BgmTvApiClient::class.java)
        Mockito.`when`(bgmTvApiClient.execute(Mockito.any<BangumiRequest<Subject>>()))
            .thenReturn(
                R(Subject(290980, "向山进发 Next Summit", "向山进发 Next Summit",
                    LocalDate.of(2022, 10, 4), 12))
            )

        val mikanVariableProvider = MikanVariableProvider(mikanSupport = mikanSupport, bgmTvClient = bgmTvApiClient)
        val sourceItem = SourceItem(
            "[ANi] 前進吧！登山少女  Next Summit（僅限港澳台地區） - 11 [1080P][Bilibili][WEB-DL][AAC AVC][CHT CHS]",
            URI("https://mikanani.me/Home/Episode/324b70bd8a170bcf13d7f5bdf9d3e8df4065f682"),
            LocalDateTime.now(), "application/x-bittorrent",
            URI("https://mikanani.me/Download/20221214/324b70bd8a170bcf13d7f5bdf9d3e8df4065f682.torrent")
        )
        val sourceGroup = mikanVariableProvider.createSourceGroup(sourceItem)
        val sourceFile = SourceFile(Path("[ANi] 前進吧！登山少女  Next Summit（僅限港澳台地區） - 11 [1080P][Bilibili][WEB-DL][AAC AVC][CHT CHS].mp4"))
        val sourceFiles = sourceGroup.filePatternVariables(sourceFile)
        assertEquals(1, sourceFiles.size)

        val bangumiInfo = sourceGroup.sharedPatternVariables() as BangumiInfo
        assertEquals("04", bangumiInfo.season)
    }

    @Test
    @Disabled
    fun test() {
        val mikanVariableProvider = MikanVariableProvider()
        val sourceItem = SourceItem(
            "[ANi] 前進吧！登山少女  Next Summit（僅限港澳台地區） - 11 [1080P][Bilibili][WEB-DL][AAC AVC][CHT CHS]",
            URI("https://mikanani.me/Home/Episode/324b70bd8a170bcf13d7f5bdf9d3e8df4065f682"),
            LocalDateTime.now(), "application/x-bittorrent",
            URI("https://mikanani.me/Download/20221214/324b70bd8a170bcf13d7f5bdf9d3e8df4065f682.torrent")
        )
        val sourceGroup = mikanVariableProvider.createSourceGroup(sourceItem)
        val sourceFiles = sourceGroup.filePatternVariables(SourceFile(Path("[ANi] 前進吧！登山少女  Next Summit（僅限港澳台地區） - 11 [1080P][Bilibili][WEB-DL][AAC AVC][CHT CHS].mp4")))
        assertEquals(1, sourceFiles.size)

        val bangumiInfo = sourceGroup.sharedPatternVariables() as BangumiInfo
        assertEquals("04", bangumiInfo.season)
    }
}