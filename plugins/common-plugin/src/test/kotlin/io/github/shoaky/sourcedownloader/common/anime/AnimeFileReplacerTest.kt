package io.github.shoaky.sourcedownloader.common.anime

import io.github.shoaky.sourcedownloader.sdk.FixedItemContent
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sourceItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

class AnimeFileReplacerTest {

    @Test
    fun test() {
        val content1 =
            FixedItemContent(
                sourceItem(title = "[DMG&VCB-S][Saki Zenkoku Hen][02][Hi10p_1080p][x264_flac].mkv"),
                emptyList()
            )
        assertEquals(false, AnimeReplacementDecider.isReplace(
            content1, null, SourceFile(Path(""))
        ))

        val content2 =
            FixedItemContent(
                sourceItem(title = "[DMG&VCB-S][Saki Zenkoku Hen][02][v2][Hi10p_1080p][x264_flac].mkv"),
                emptyList()
            )
        assertEquals(true, AnimeReplacementDecider.isReplace(
            content2, null, SourceFile(Path(""))
        ))
    }
}