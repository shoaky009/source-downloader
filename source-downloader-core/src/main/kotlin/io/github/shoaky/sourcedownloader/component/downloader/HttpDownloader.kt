package io.github.shoaky.sourcedownloader.component.downloader

import io.github.shoaky.sourcedownloader.sdk.DownloadTask
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.Downloader
import io.github.shoaky.sourcedownloader.sdk.util.http.httpClient
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.*
import java.nio.ByteBuffer
import java.nio.file.Path

class HttpDownloader(
    private val downloadPath: Path,
    private val client: HttpClient = httpClient,
) : Downloader {

    override fun submit(task: DownloadTask) {
        val uris = if (task.options.preferFileUri) {
            task.downloadFiles.mapNotNull { it.fileUri }
        } else {
            listOf(task.sourceItem.downloadUri)
        }

        uris.map {
            val request = HttpRequest.newBuilder(it).GET()
            request.build()
        }.forEach {
            val monitorableBodyHandle = MonitorableBodyHandle(
                BodyHandlers.ofFileDownload(task.downloadPath)
            )
            client.send(
                it,
                monitorableBodyHandle
            )
        }
    }

    override fun defaultDownloadPath(): Path {
        return downloadPath
    }

    override fun cancel(sourceItem: SourceItem) {
        TODO("Not yet implemented")
    }
}

class MonitorableBodyHandle<T>(
    private val bodyHandler: BodyHandler<T>
) : BodyHandler<T> by bodyHandler {

    private var monitor: MonitorableBodySubscriber<T>? = null

    override fun apply(responseInfo: ResponseInfo): BodySubscriber<T> {
        val bodySubscriber = MonitorableBodySubscriber(bodyHandler.apply(responseInfo))
        monitor = bodySubscriber
        return bodySubscriber
    }
}

class MonitorableBodySubscriber<T>(
    private val delegate: BodySubscriber<T>
) : BodySubscriber<T> by delegate {

    private val startTime: Long = System.currentTimeMillis()
    private var downloadedSize: Long = 0

    override fun onNext(item: List<ByteBuffer>) {
        downloadedSize += item.size.toLong()
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - startTime
        val downloadSpeed = downloadedSize.toDouble() / elapsedTime
        println("Downloaded: $downloadedSize bytes")
        println("Download Speed: $downloadSpeed bytes/ms")
        delegate.onNext(item)
    }

}