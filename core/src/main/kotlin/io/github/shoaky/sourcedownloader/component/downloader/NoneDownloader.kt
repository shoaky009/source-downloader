package io.github.shoaky.sourcedownloader.component.downloader

import io.github.shoaky.sourcedownloader.sdk.DownloadTask
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.Downloader
import java.nio.file.Path
import kotlin.io.path.Path

class NoneDownloader(
    val downloadPath: Path = Path("").toAbsolutePath()
) : Downloader {

    override fun submit(task: DownloadTask): Boolean {
        return true
    }

    override fun defaultDownloadPath(): Path {
        return downloadPath
    }

    override fun cancel(sourceItem: SourceItem, files: List<SourceFile>) {
    }

}