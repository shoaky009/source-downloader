package io.github.shoaky.sourcedownloader.component.downloader

import io.github.shoaky.sourcedownloader.sdk.DownloadTask
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.Downloader
import org.springframework.core.io.UrlResource
import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.createDirectories

/**
 * URL下载器，通过SourceItem中的downloadUri下载文件
 */
class UrlDownloader(
    private val downloadPath: Path
) : Downloader {

    override fun submit(task: DownloadTask): Boolean {
        val uriResource = UrlResource(task.downloadUri())

        val readableByteChannel = Channels.newChannel(uriResource.inputStream)
        task.downloadFiles.associateBy { it.path.parent }
            .forEach {
                it.key.createDirectories()
            }

        task.downloadFiles.forEach { file ->
            FileChannel.open(file.path,
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE
            ).use {
                it.transferFrom(readableByteChannel, 0, Long.MAX_VALUE)
            }
        }
        return true
    }

    override fun defaultDownloadPath(): Path {
        return downloadPath
    }

    override fun cancel(sourceItem: SourceItem, files: List<SourceFile>) {
        TODO("Not yet implemented")
    }

}

