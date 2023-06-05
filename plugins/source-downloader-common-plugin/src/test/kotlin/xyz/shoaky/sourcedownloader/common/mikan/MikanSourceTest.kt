package xyz.shoaky.sourcedownloader.common.mikan

import com.apptasticsoftware.rssreader.Item
import com.apptasticsoftware.rssreader.RssReader
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import xyz.shoaky.sourcedownloader.common.anime.MikanPointer
import xyz.shoaky.sourcedownloader.common.anime.MikanSource
import xyz.shoaky.sourcedownloader.common.anime.MikanSupport
import xyz.shoaky.sourcedownloader.sdk.PointedItem
import xyz.shoaky.sourcedownloader.sdk.util.Jackson
import java.net.URI
import java.time.LocalDateTime
import kotlin.io.path.Path
import kotlin.io.path.readText
import kotlin.test.assertEquals

class MikanSourceTest {

    private val testDataPath = Path("src", "test", "resources", "mikan")

    private val topItemData = Jackson.fromJson(
        testDataPath.resolve("rss-data.json").readText(),
        jacksonTypeRef<List<Item>>()
    )

    @Test
    fun given_max_datetime_should_empty() {
        val rssReader = Mockito.mock(RssReader::class.java)
        val source = MikanSource("", true, rssReader = rssReader)
        Mockito.`when`(rssReader.read(Mockito.anyString()))
            .thenAnswer { topItemData.stream() }

        val list = source.fetch(MikanPointer(
            LocalDateTime.MAX
        )).toList()
        assert(list.isEmpty())
    }

    @Test
    fun given_normal_datetime() {
        val rssReader = Mockito.mock(RssReader::class.java)
        val source = MikanSource(
            "", false,
            rssReader = rssReader,
        )

        val targetDate = LocalDateTime.parse("2023-05-16T23:31:58.786")
        val items = source.fetch(MikanPointer(
            targetDate
        ))
        assert(items.all { it.sourceItem.date > targetDate })
    }

    @Test
    fun test() {
        val rssReader = Mockito.mock(RssReader::class.java)
        Mockito.`when`(rssReader.read("http://topItems"))
            .thenAnswer {
                topItemData.stream()
            }

        Mockito.`when`(rssReader.read("https://mikanani.me/RSS/Bangumi?bangumiId=2906&subgroupid=583"))
            .thenAnswer {
                Jackson.fromJson(
                    testDataPath.resolve("2906-data.json").readText(),
                    jacksonTypeRef<List<Item>>()
                ).stream()
            }
        Mockito.`when`(rssReader.read("https://mikanani.me/RSS/Bangumi?bangumiId=2976&subgroupid=583"))
            .thenAnswer {
                Jackson.fromJson(
                    testDataPath.resolve("2976-data.json").readText(),
                    jacksonTypeRef<List<Item>>()
                ).stream()
            }

        val support = Mockito.mock(MikanSupport::class.java)
        Mockito.`when`(support.getEpisodePageInfo(
            URI("https://mikanani.me/Home/Episode/fd1a9d6157ff60052ec223586745278dd1d6c9fa")
        )
        ).thenReturn(MikanSupport.EpisodePageInfo(fansubRss = "https://mikanani.me/RSS/Bangumi?bangumiId=2906&subgroupid=583"))

        Mockito.`when`(support.getEpisodePageInfo(
            URI("https://mikanani.me/Home/Episode/38765370c4058c8d928f97d4c0f3f93564762aa6")
        )
        ).thenReturn(MikanSupport.EpisodePageInfo(fansubRss = "https://mikanani.me/RSS/Bangumi?bangumiId=2976&subgroupid=583"))

        Mockito.`when`(support.getEpisodePageInfo(
            URI("https://mikanani.me/Home/Episode/e3a592a221d70310e6f576f39303d63618e4dc4c"),
        )
        ).thenReturn(MikanSupport.EpisodePageInfo())

        val mikanSource = MikanSource("http://topItems", true, rssReader, support)

        val result = mutableListOf<PointedItem<MikanPointer>>()
        val limit = 1
        var sourceItems = mikanSource.fetch(MikanPointer(LocalDateTime.MIN), limit).toList()
        while (sourceItems.isNotEmpty()) {
            val first = sourceItems.first()
            result.addAll(sourceItems)
            sourceItems = mikanSource.fetch(first.pointer, limit).toList()
        }

        assertEquals(15, result.size)
    }
}