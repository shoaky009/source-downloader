package io.github.shoaky.sourcedownloader.telegram

import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.nio.ByteBuffer
import java.nio.channels.SeekableByteChannel
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProgressiveChannelTest {

    @Test
    fun test() {
        val mock = Mockito.mock(SeekableByteChannel::class.java)
        Mockito.`when`(mock.write(Mockito.any())).thenReturn(1024 * 1024 * 100)
        val ch = ProgressiveChannel(3L * (1024 * 1024 * 1024), mock)
        ch.write(ByteBuffer.wrap(ByteArray(0)))

        println(ch.formatRate())
        assertTrue(ch.formatRate().contains("MiB"))
        assertEquals("3%", ch.formatProgress())
    }
}