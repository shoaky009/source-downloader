package io.github.shoaky.sourcedownloader.component.downloader

import io.github.shoaky.sourcedownloader.sdk.DownloadTask
import io.github.shoaky.sourcedownloader.sdk.component.Downloader
import org.springframework.core.io.UrlResource
import java.io.FileOutputStream
import java.nio.channels.Channels
import java.nio.file.Path

class UrlDownloader(
    private val downloadPath: Path
) : Downloader {

    override fun submit(task: DownloadTask) {
        val uriResource = UrlResource(task.downloadUri())
        val filename = uriResource.filename.takeIf { it.isNullOrBlank().not() }
            ?: task.sourceItem.hashing()
        val dp = task.downloadPath

        val targetPath = dp.resolve(filename)
        val readableByteChannel = Channels.newChannel(uriResource.inputStream)
        FileOutputStream(targetPath.toFile()).use {
            it.channel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE)
        }
    }

    override fun defaultDownloadPath(): Path {
        return downloadPath
    }

}

