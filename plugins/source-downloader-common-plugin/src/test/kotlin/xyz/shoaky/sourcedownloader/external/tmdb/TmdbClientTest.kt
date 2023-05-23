package xyz.shoaky.sourcedownloader.external.tmdb

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.sdk.util.Jackson

@Disabled
class TmdbClientTest {

    private val client = TmdbClient(TmdbClient.DEFAULT_TOKEN)

    @Test
    fun search_tv_show() {
        val execute = client.execute(SearchTvShow("鬼滅の刃 刀鍛冶の里編"))
        execute.body().results.forEach {
            println(Jackson.toJsonString(it))
        }
    }

    @Test
    fun get_tv_show() {
        val execute = client.execute(GetTvShow(85937))
        println(execute.body())
    }
}