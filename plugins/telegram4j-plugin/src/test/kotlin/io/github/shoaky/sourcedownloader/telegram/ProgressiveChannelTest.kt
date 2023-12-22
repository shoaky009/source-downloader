package io.github.shoaky.sourcedownloader.telegram

import io.github.shoaky.sourcedownloader.telegram.util.ProgressiveChannel
import org.junit.jupiter.api.Test
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProgressiveChannelTest {

    @Test
    fun test() {
        val tempFile = Files.createTempFile(null, null)
        val ch = ProgressiveChannel(
            3L * (1024 * 1024), FileChannel.open(
                tempFile,
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE,
                StandardOpenOption.DELETE_ON_CLOSE
            )
        )

        for (i in 0 until 100) {
            ch.write(ByteBuffer.allocate(1024))
        }

        println(ch.formatRate())
        assertTrue(ch.formatRate().contains("KiB"))
        assertEquals("3%", ch.formatProgress())
    }
}