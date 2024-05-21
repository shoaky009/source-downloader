package io.github.shoaky.sourcedownloader.common.external.season

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.common.torrent.R
import io.github.shoaky.sourcedownloader.external.season.*
import io.github.shoaky.sourcedownloader.external.tmdb.*
import io.github.shoaky.sourcedownloader.sdk.util.Jackson
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.test.assertEquals

@Disabled("参数匹配的方法需要调整")
class SeasonChainTest {

    @Test
    fun should_all_expected() {
        val tmdbClient = Mockito.mock(TmdbClient::class.java)

        val seasonSupport = SeasonSupport(
            listOf(
                GeneralSeasonParser,
                LastStringSeasonParser,
                TmdbSeasonParser(tmdbClient)
            ),
            withDefault = true
        )

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
                if (it.tmdbSearchMockData != null) {
                    val split = it.name.split(" ")
                    val search = split.first()
                    Mockito.`when`(tmdbClient.execute(Mockito.eq(SearchTvShow(search))))
                        .thenReturn(R(pageResult(it.tmdbSearchMockData)))
                } else {
                    Mockito.`when`(tmdbClient.execute(Mockito.eq(SearchTvShow(""))))
                        .thenReturn(R(pageResult(emptyList())))
                }
                if (it.tmdbTvShowMockData != null) {
                    val data = it.tmdbTvShowMockData
                    Mockito.`when`(tmdbClient.execute(Mockito.eq(GetTvShow(data.id))))
                        .thenReturn(R(data))
                }

                val output = seasonSupport.input(
                    ParseValue(it.filename, listOf(0)),
                    ParseValue(it.name)
                )
                assertEquals(it.expect, output, "name:${it.name}")
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
    return PageResult(1, results, 1, 1)
}