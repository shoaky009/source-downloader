package io.github.shoaky.sourcedownloader.external.anilist

import io.github.shoaky.sourcedownloader.sdk.util.Jackson
import org.junit.jupiter.api.Test

// @Disabled
class AnilistClientTest {

    @Test
    fun test() {
        val anilistClient = AnilistClient()
        val execute = anilistClient.execute(Search("Tate no Yuusha no Nariagari Season 02"))
        println(Jackson.toJsonString(execute.body().data.page.medias))
    }

}