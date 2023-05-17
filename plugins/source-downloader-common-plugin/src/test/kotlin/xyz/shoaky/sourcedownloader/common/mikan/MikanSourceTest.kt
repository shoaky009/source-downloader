package xyz.shoaky.sourcedownloader.common.mikan

import com.apptasticsoftware.rssreader.Item
import com.apptasticsoftware.rssreader.RssReader
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import xyz.shoaky.sourcedownloader.sdk.util.Jackson
import java.time.LocalDateTime
import kotlin.io.path.Path
import kotlin.io.path.readText

class MikanSourceTest {

    private val items = Jackson.fromJson(
        Path("src", "test", "resources", "mikan", "rss-data.json").readText(),
        jacksonTypeRef<List<Item>>()
    )

    @Test
    fun given_max_datetime_should_empty() {
        val rssReader = Mockito.mock(RssReader::class.java)
        val source = MikanSource("", true, rssReader = rssReader)
        Mockito.`when`(rssReader.read(Mockito.anyString()))
            .thenAnswer { items.stream() }

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
            extractor = MikanSubGroupItemExtractor(
                rssReader, MikanSupport("token")
            )
        )

        val targetDate = LocalDateTime.parse("2023-05-16T23:31:58.786")
        val toList = source.fetch(MikanPointer(
            targetDate
        ))
        assert(toList.all { it.sourceItem.date > targetDate })
    }
}