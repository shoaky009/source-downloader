package xyz.shoaky.sourcedownloader.tagger

import org.junit.jupiter.api.Test
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
    }
}