package io.github.shoaky.sourcedownloader.telegram.util

import java.nio.ByteBuffer
import java.nio.channels.ByteChannel
import java.nio.channels.SeekableByteChannel
import java.text.NumberFormat
import java.time.Duration
import java.time.Instant

class ProgressiveChannel(
    private val totalSize: Long,
    private val ch: SeekableByteChannel
) : ByteChannel by ch {

    private var downloadedBytes = 0L
    private val startTime = Instant.now()

    override fun write(src: ByteBuffer): Int {
        val write = ch.write(src)
        downloadedBytes += write
        return write
    }

    fun formatProgress(): String {
        return NumberFormat.getPercentInstance().format(downloadedBytes.toDouble() / totalSize.toDouble())
    }

    fun formatRate(): String {
        val curr = Instant.now().epochSecond
        val rate = if (curr == startTime.epochSecond) {
            downloadedBytes
        } else {
            downloadedBytes / (curr - startTime.epochSecond)
        }

        return when {
            rate > GIGABYTE -> {
                String.format("%.2f GiB/s", rate / GIGABYTE)
            }

            rate > MEGABYTE -> {
                String.format("%.2f MiB/s", rate / MEGABYTE)
            }

            rate > KILOBYTE -> {
                String.format("%.2f KiB/s", rate / KILOBYTE)
            }

            else -> {
                "$rate B/s"
            }
        }
    }

    fun formatTotalSize(): String {
        return when {
            totalSize > GIGABYTE -> {
                String.format("%.2f GiB", totalSize / GIGABYTE)
            }

            totalSize > MEGABYTE -> {
                String.format("%.2f MiB", totalSize / MEGABYTE)
            }

            totalSize > KILOBYTE -> {
                String.format("%.2f KiB", totalSize / KILOBYTE)
            }

            else -> {
                "$totalSize B"
            }
        }
    }

    fun getDuration(): Long {
        return Duration.ofSeconds(Instant.now().epochSecond - startTime.epochSecond).seconds
    }

    override fun close() {
        ch.close()
    }

    companion object {

        private const val KILOBYTE = 1024.0
        private const val MEGABYTE = KILOBYTE * 1024.0
        private const val GIGABYTE = MEGABYTE * 1024.0
    }
}