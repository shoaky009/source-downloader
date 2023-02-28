package xyz.shoaky.sourcedownloader.core.component

import xyz.shoaky.sourcedownloader.sdk.DownloadTask
import xyz.shoaky.sourcedownloader.sdk.component.Downloader
import java.nio.file.Path

class HttpDownloader : Downloader {

    override fun submit(task: DownloadTask) {
        TODO("Not yet implemented")
    }

    override fun defaultDownloadPath(): Path {
        return Path.of("")
    }

}