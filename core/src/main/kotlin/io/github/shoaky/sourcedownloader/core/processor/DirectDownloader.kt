package io.github.shoaky.sourcedownloader.core.processor

import io.github.shoaky.sourcedownloader.sdk.DownloadTask
import io.github.shoaky.sourcedownloader.sdk.component.Downloader
import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption

class DirectDownloader(
    private val downloader: Downloader
) : Downloader by downloader {

    override fun submit(task: DownloadTask): Boolean {
        // 没有考虑网盘的情况，如果需要支持需要重新设计
        val (normalFiles, directDownloadFiles) = task.downloadFiles.partition { it.data == null }
        directDownloadFiles
            .mapNotNull { file ->
                file.data?.let {
                    file to Channels.newChannel(it)
                }
            }.forEach { (file, channel) ->
                FileChannel.open(
                    file.path,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE
                ).use {
                    it.transferFrom(channel, 0, Long.MAX_VALUE)
                }
            }

        return downloader.submit(task.copy(downloadFiles = normalFiles))
    }
}