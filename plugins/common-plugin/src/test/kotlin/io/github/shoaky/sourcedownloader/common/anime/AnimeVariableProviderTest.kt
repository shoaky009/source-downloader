package io.github.shoaky.sourcedownloader.common.anime

import io.github.shoaky.sourcedownloader.external.anilist.AnilistClient
import io.github.shoaky.sourcedownloader.external.bangumi.BgmTvApiClient
import io.github.shoaky.sourcedownloader.sourceItem
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.test.assertEquals

class AnimeVariableProviderTest {

    @Test
    @Disabled
    fun test() {
        val provider = AnimeVariableProvider(
            BgmTvApiClient(),
            AnilistClient(),
            true
        )
        val sharedVariables = provider.itemVariables(
            sourceItem(
                "约会大作战"
            )
        )
        println(sharedVariables.variables())
    }

    // TODO MOCK
    // @Test
    fun extract_title() {
        val provider = AnimeVariableProvider(
            BgmTvApiClient(),
            AnilistClient()
        )

        Files.readAllLines(Path("src", "test", "resources", "raw-anime-title-data.csv"))
            .map {
                val split = it.split(",")
                split[0] to split[1]
            }.forEach { (title, expect) ->
                val extractTitle = provider.extractFrom(sourceItem(), title)
                assertEquals(expect, extractTitle?.variables()?.get("nativeName"))
            }
    }
}