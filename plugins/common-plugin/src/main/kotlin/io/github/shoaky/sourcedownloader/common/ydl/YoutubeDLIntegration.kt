package io.github.shoaky.sourcedownloader.common.ydl

import io.github.shoaky.sourcedownloader.external.ydl.*
import io.github.shoaky.sourcedownloader.sdk.DownloadTask
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.AsyncDownloader
import io.github.shoaky.sourcedownloader.sdk.component.ItemFileResolver
import io.github.shoaky.sourcedownloader.sdk.http.StatusCodes
import java.nio.file.Path
import kotlin.io.path.Path

class YoutubeDLIntegration(
    private val client: YoutubeDLClient,
    private val downloadPath: Path,
) : ItemFileResolver, AsyncDownloader {

    override fun resolveFiles(sourceItem: SourceItem): List<SourceFile> {
        val response = client.execute(
            GetFileFormats(
                sourceItem.link.toURL()
            )
        )
        val result = response.body().result
        return listOf(
            SourceFile(
                Path(result.filename)
            )
        )
    }

    override fun isFinished(sourceItem: SourceItem): Boolean? {
        // 看看有没有根据url查询的
        val info = client.execute(GetDownloads()).body().downloads.firstOrNull {
            it.url.startsWith(sourceItem.link.toString())
        } ?: return null
        return info.percentComplete == 100.0
    }

    override fun submit(task: DownloadTask): Boolean {
        val response = client.execute(
            SubmitDownload(
                task.sourceItem.link.toURL()
            )
        )
        return response.statusCode() == StatusCodes.OK
    }

    override fun defaultDownloadPath(): Path {
        return downloadPath
    }

    override fun cancel(sourceItem: SourceItem, files: List<SourceFile>) {
        val info = client.execute(GetDownloads()).body().downloads.firstOrNull {
            it.url.startsWith(sourceItem.link.toString())
        } ?: return
        client.execute(CancelDownload(info.uid))
    }
}