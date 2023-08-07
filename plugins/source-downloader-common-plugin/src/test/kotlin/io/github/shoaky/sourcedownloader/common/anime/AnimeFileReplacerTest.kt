package io.github.shoaky.sourcedownloader.common.anime

import io.github.shoaky.sourcedownloader.sdk.FixedItemContent
import io.github.shoaky.sourcedownloader.sourceItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AnimeFileReplacerTest {

    @Test
    fun test() {
        assertEquals(false, AnimeReplacementDecider.isReplace(
            FixedItemContent(sourceItem(title = "[DMG&VCB-S][Saki Zenkoku Hen][02][Hi10p_1080p][x264_flac].mkv"), emptyList()), null
        ))

        assertEquals(true, AnimeReplacementDecider.isReplace(
            FixedItemContent(sourceItem(title = "[DMG&VCB-S][Saki Zenkoku Hen][02][v2][Hi10p_1080p][x264_flac].mkv"), emptyList()), null
        ))
    }
}