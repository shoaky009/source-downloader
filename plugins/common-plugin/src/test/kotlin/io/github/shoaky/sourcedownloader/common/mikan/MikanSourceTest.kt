package io.github.shoaky.sourcedownloader.common.mikan

import com.apptasticsoftware.rssreader.Item
import com.apptasticsoftware.rssreader.RssReader
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.common.anime.FansubPointer
import io.github.shoaky.sourcedownloader.common.anime.MikanClient
import io.github.shoaky.sourcedownloader.common.anime.MikanPointer
import io.github.shoaky.sourcedownloader.common.anime.MikanSource
import io.github.shoaky.sourcedownloader.sdk.ItemPointer
import io.github.shoaky.sourcedownloader.sdk.PointedItem
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.util.Jackson
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.net.URI
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.io.path.Path
import kotlin.io.path.readText
import kotlin.test.assertEquals

class MikanSourceTest {

    private val testDataPath = Path("src", "test", "resources", "mikan")
    private val zoneId = ZoneId.of("Asia/Shanghai")

    private val topItemData = Jackson.fromJson(
        testDataPath.resolve("rss-data.json").readText(),
        jacksonTypeRef<List<Item>>()
    )

    @Test
    fun given_max_datetime_should_return_origin() {
        val rssReader = Mockito.mock(RssReader::class.java)
        val source = MikanSource("", true, rssReader = rssReader)
        Mockito.`when`(rssReader.read(Mockito.anyString()))
            .thenAnswer { topItemData.stream() }

        val list = source.fetch(
            MikanPointer(
                OffsetDateTime.MAX
            )
        ).toList()
        assertEquals(topItemData.size, list.size)
    }

    @Test
    fun given_normal_datetime() {
        val rssReader = Mockito.mock(RssReader::class.java)
        val source = MikanSource(
            "", false,
            rssReader = rssReader,
        )

        val targetDate = LocalDateTime.parse("2023-05-16T23:31:58.786").atOffset(SourceItem.DEFAULT_OFFSET)
        val items = source.fetch(
            MikanPointer(
                targetDate
            )
        )
        assert(items.all { it.sourceItem.datetime > targetDate })
    }

    @Test
    fun given_default_pointer() {
        val mikanPointer = MikanPointer(OffsetDateTime.MIN)
        val result = pointedItems(mikanPointer, 50)
        assertEquals(16, result.size)
    }

    @Test
    fun given_state_pointer() {
        val mikanPointer = MikanPointer(
            LocalDateTime.of(2023, 5, 17, 0, 0).atOffset(ZoneOffset.ofHours(8)),
            mutableMapOf(
                "2976-583" to LocalDateTime.of(2023, 5, 10, 0, 0).atOffset(ZoneOffset.ofHours(8))
            )
        )
        val result = pointedItems(mikanPointer, 5)
        result.forEach {
            println(it)
        }
        assertEquals(3, result.size)
    }

    private fun pointedItems(mikanPointer: MikanPointer, limit: Int): MutableList<PointedItem<ItemPointer>> {
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
        Mockito.`when`(rssReader.read("https://mikanani.me/RSS/Bangumi?bangumiId=2994&subgroupid=604"))
            .thenAnswer {
                emptyList<List<Item>>().stream()
            }

        val support = Mockito.mock(MikanClient::class.java)
        Mockito.`when`(
            support.getEpisodePageInfo(
                URI("https://mikanani.me/Home/Episode/fd1a9d6157ff60052ec223586745278dd1d6c9fa").toURL()
            )
        )
            .thenReturn(MikanClient.EpisodePageInfo(fansubRss = "https://mikanani.me/RSS/Bangumi?bangumiId=2906&subgroupid=583"))

        Mockito.`when`(
            support.getEpisodePageInfo(
                URI("https://mikanani.me/Home/Episode/38765370c4058c8d928f97d4c0f3f93564762aa6").toURL()
            )
        )
            .thenReturn(MikanClient.EpisodePageInfo(fansubRss = "https://mikanani.me/RSS/Bangumi?bangumiId=2976&subgroupid=583"))

        Mockito.`when`(
            support.getEpisodePageInfo(
                URI("https://mikanani.me/Home/Episode/e3a592a221d70310e6f576f39303d63618e4dc4c").toURL(),
            )
        )
            .thenReturn(MikanClient.EpisodePageInfo(fansubRss = "https://mikanani.me/RSS/Bangumi?bangumiId=2994&subgroupid=604"))

        val mikanSource = MikanSource("http://topItems", true, rssReader, support, false)

        val result = mutableListOf<PointedItem<ItemPointer>>()
        val sourceItems = mikanSource.fetch(mikanPointer, limit)
        for (sourceItem in sourceItems) {
            val fansubPointer = sourceItem.pointer as FansubPointer
            mikanPointer.update(fansubPointer)
            result.add(sourceItem)
        }
        return result
    }

}