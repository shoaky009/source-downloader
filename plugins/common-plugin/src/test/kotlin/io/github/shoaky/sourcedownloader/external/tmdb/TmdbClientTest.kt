package io.github.shoaky.sourcedownloader.external.tmdb

import io.github.shoaky.sourcedownloader.sdk.util.Jackson
import org.junit.jupiter.api.Test

// @Disabled
class TmdbClientTest {

    private val client = TmdbClient(TmdbClient.DEFAULT_TOKEN)

    @Test
    fun search_tv_show() {
        val execute = client.execute(SearchTvShow("けものフレンズ"))
        execute.body().results.forEach {
            println(Jackson.toJsonString(it))
        }
    }

    @Test
    fun get_tv_show() {
        val execute = client.execute(GetTvShow(69288))
        println(Jackson.toJsonString(execute.body()))
    }
}