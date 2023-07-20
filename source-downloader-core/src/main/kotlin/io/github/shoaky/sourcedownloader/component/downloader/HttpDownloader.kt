package io.github.shoaky.sourcedownloader.component.downloader

import io.github.shoaky.sourcedownloader.sdk.DownloadTask
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.Downloader
import io.github.shoaky.sourcedownloader.sdk.util.http.httpClient
import java.net.http.HttpClient
import java.nio.file.Path

class HttpDownloader(
    private val downloadPath: Path,
    private val client: HttpClient = httpClient
) : Downloader {

    override fun submit(task: DownloadTask) {
        task.options.tags
    }

    override fun defaultDownloadPath(): Path {
        return downloadPath
    }

    override fun cancel(sourceItem: SourceItem) {
        TODO("Not yet implemented")
    }
}