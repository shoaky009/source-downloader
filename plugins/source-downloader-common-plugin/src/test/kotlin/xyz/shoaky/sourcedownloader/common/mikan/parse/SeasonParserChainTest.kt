package xyz.shoaky.sourcedownloader.common.mikan.parse

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import xyz.shoaky.sourcedownloader.common.torrent.R
import xyz.shoaky.sourcedownloader.external.bangumi.Subject
import xyz.shoaky.sourcedownloader.external.tmdb.*
import xyz.shoaky.sourcedownloader.sdk.util.Jackson
import java.nio.file.Files
import java.time.LocalDate
import kotlin.io.path.Path
import kotlin.test.assertEquals

fun create(name: String): SubjectContent {
    val subject = Subject(1, name, name, LocalDate.now(), 12)
    return SubjectContent(subject, name)
}

class SeasonParserChainTest {

    @Test
    fun should_all_expected() {
        val tmdbClient = Mockito.mock(TmdbClient::class.java)

        // val tmdbClient = TmdbClient(TmdbClient.DEFAULT_TOKEN)

        val parserChain = ParserChain(listOf(
            CommonSeasonParser,
            TmdbSeasonParser(tmdbClient),
            DefaultValueSeasonParser
        ), true)

        val mockingDetails = Mockito.mockingDetails(tmdbClient)

        Files.readAllLines(Path("src", "test", "resources", "season-test.csv"))
            .filter { it.isNullOrBlank().not() }
            .map { line ->
                val split = line.split("|")
                MockData(
                    split[0].toInt(),
                    split[1],
                    split.elementAtOrNull(2) ?: "",
                    split.elementAtOrNull(3)?.let {
                        if (it.isBlank()) {
                            return@let emptyList()
                        }
                        Jackson.fromJson(it, jacksonTypeRef())
                    },
                    split.elementAtOrNull(4)?.let {
                        if (it.isBlank()) {
                            return@let null
                        }
                        Jackson.fromJson(it, jacksonTypeRef())
                    }
                )
            }
            .forEach {
                if (mockingDetails.isMock) {
                    if (it.tmdbSearchMockData != null) {
                        val split = it.name.split(" ")
                        val search = split.first()
                        Mockito.`when`(tmdbClient.execute(Mockito.eq(SearchTvShow(search))))
                            .thenReturn(R(pageResult(it.tmdbSearchMockData)))
                    }
                    if (it.tmdbTvShowMockData != null) {
                        val data = it.tmdbTvShowMockData
                        Mockito.`when`(tmdbClient.execute(Mockito.eq(GetTvShow(data.id))))
                            .thenReturn(R(data))
                    }
                }

                val name = it.name
                val season = parserChain.apply(create(name), it.filename).value
                assertEquals(it.expect, season, "name:${name}")
            }
    }

}

private class MockData(
    val expect: Int,
    val name: String,
    val filename: String,
    val tmdbSearchMockData: List<SearchResult>?,
    val tmdbTvShowMockData: TvShow?,
)

private fun pageResult(results: List<SearchResult>): PageResult<SearchResult> {
    return PageResult(
        1,
        results,
        1,
        1)
}