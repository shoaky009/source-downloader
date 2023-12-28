package io.github.shoaky.sourcedownloader.common.tagger

import io.github.shoaky.sourcedownloader.common.supplier.SimpleFileTaggerSupplier
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SimpleFileTaggerTest {

    private val tagger = SimpleFileTaggerSupplier.apply(CoreContext.empty, Properties.empty)

    @Test
    fun common_extension() {
        assertEquals("image", tagger.tag("test.jpg"))
        assertEquals("audio", tagger.tag("test.mp3"))
        assertEquals("video", tagger.tag("test.mp4"))
        assertEquals("video", tagger.tag("test.mkv"))
        assertEquals("video", tagger.tag("test.avi"))
    }

    @Test
    fun expansion() {
        assertEquals("subtitle", tagger.tag("test.ass"))
        assertEquals("subtitle", tagger.tag("test.srt"))
        assertEquals("subtitle", tagger.tag("test.ssa"))
        assertEquals("subtitle", tagger.tag("[UHA-WINGS&VCB-Studio] Karakai Jouzu no Takagi-san 2 [04][Ma10p_1080p][x265_flac].tc.ass"))
    }
}