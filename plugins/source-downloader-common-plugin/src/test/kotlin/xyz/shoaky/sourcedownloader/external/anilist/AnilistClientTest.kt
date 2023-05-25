package xyz.shoaky.sourcedownloader.external.anilist

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.sdk.util.Jackson

@Disabled
class AnilistClientTest {

    @Test
    fun test() {
        val anilistClient = AnilistClient()
        val execute = anilistClient.execute(Search("Tate no Yuusha no Nariagari Season 02"))
        println(Jackson.toJsonString(execute.body().data.page.medias))
    }

}