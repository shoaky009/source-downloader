package io.github.shoaky.sourcedownloader.common.anime

import io.github.shoaky.sourcedownloader.external.anilist.AnilistClient
import io.github.shoaky.sourcedownloader.external.bangumi.BgmTvApiClient
import io.github.shoaky.sourcedownloader.sourceItem
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Disabled
class AnimeVariableProviderTest {

    @Test
    fun test() {
        val provider = AnimeVariableProvider(
            BgmTvApiClient(),
            AnilistClient(),
            true
        )
        val createSourceGroup = provider.createSourceGroup(
            sourceItem(
                "てーきゅう 9期"
            )
        )
        println(createSourceGroup.sharedPatternVariables().variables())
    }
}