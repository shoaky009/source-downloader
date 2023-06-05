package xyz.shoaky.sourcedownloader.common.tagger

import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.common.supplier.SimpleFileTaggerSupplier
import xyz.shoaky.sourcedownloader.sdk.Properties
import kotlin.test.assertEquals

class SimpleFileTaggerTest {
    @Test
    fun common_extension() {
        val tagger = SimpleFileTaggerSupplier.apply(Properties.EMPTY)
        assertEquals("image", tagger.tag("test.jpg"))
        assertEquals("audio", tagger.tag("test.mp3"))
        assertEquals("video", tagger.tag("test.mp4"))
        assertEquals("video", tagger.tag("test.mkv"))
        assertEquals("video", tagger.tag("test.avi"))
    }

    @Test
    fun expansion() {
        val tagger = SimpleFileTaggerSupplier.apply(Properties.EMPTY)
        assertEquals("subtitle", tagger.tag("test.ass"))
        assertEquals("subtitle", tagger.tag("test.srt"))
        assertEquals("subtitle", tagger.tag("test.ssa"))
        assertEquals("subtitle", tagger.tag("[UHA-WINGS&VCB-Studio] Karakai Jouzu no Takagi-san 2 [04][Ma10p_1080p][x265_flac].tc.ass"))
    }
}