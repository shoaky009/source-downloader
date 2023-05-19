package xyz.shoaky.sourcedownloader.external.tmdb

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Disabled
class TmdbClientV2Test {

    @Test
    fun name() {
        val client = TmdbClientV2("7d82a6a830d5f4458f42929f73878195")
        val execute = client.execute(SearchTvShow("异世界一击杀姊姊"))

        execute.body().forEach {
            println(it)
        }
    }
}