package io.github.shoaky.sourcedownloader.common.rss

import com.apptasticsoftware.rssreader.Item
import com.apptasticsoftware.rssreader.RssReader
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.common.supplier.JackettSourceSupplier
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.util.Jackson
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.Mockito
import kotlin.io.path.Path
import kotlin.io.path.readText

class JackettSourceTest {

    private val testDataPath = Path("src", "test", "resources", "rss")
    private val topItemData = Jackson.fromJson(
        testDataPath.resolve("jackett.json").readText(),
        jacksonTypeRef<List<Item>>()
    )

    @Test
    fun test() {
        val rssReader = Mockito.mock(RssReader::class.java)
        Mockito.`when`(rssReader.read(Mockito.anyString()))
            .thenAnswer { topItemData.stream() }

        val source = JackettSource("http://localhost", rssReader)
        val fetch = source.fetch()

        assertDoesNotThrow {
            fetch.first()
        }
    }

    @Test
    fun test_creation() {
        assertDoesNotThrow {
            JackettSourceSupplier.apply(
                Properties.fromMap(mapOf("url" to "http://localhost"))
            )
        }
    }
}