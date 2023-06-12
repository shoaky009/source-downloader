package io.github.shoaky.sourcedownloader.common.anime

import io.github.shoaky.sourcedownloader.external.anilist.AnilistClient
import io.github.shoaky.sourcedownloader.external.bangumi.BgmTvApiClient
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.net.URI
import java.time.LocalDateTime

@Disabled
class AnimeVariableProviderTest {

    @Test
    fun test() {
        val provider = AnimeVariableProvider(
            BgmTvApiClient(),
            AnilistClient()
        )
        val createSourceGroup = provider.createSourceGroup(SourceItem(
            "[DMG&LoliHouse] Spy x Family [01-25][WebRip 1080p HEVC-10bit AAC ASSx2]",
            URI("http://localhost"),
            LocalDateTime.now(),
            "",
            URI("http://localhost"),
        ))
        println(createSourceGroup.sharedPatternVariables().variables())
    }
}