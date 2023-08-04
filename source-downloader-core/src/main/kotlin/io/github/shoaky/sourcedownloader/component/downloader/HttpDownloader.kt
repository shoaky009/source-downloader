package io.github.shoaky.sourcedownloader.component.downloader

import io.github.shoaky.sourcedownloader.sdk.DownloadTask
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.ComponentStateful
import io.github.shoaky.sourcedownloader.sdk.component.Downloader
import io.github.shoaky.sourcedownloader.sdk.util.http.httpClient
import io.github.shoaky.sourcedownloader.sdk.util.readableRate
import org.slf4j.LoggerFactory
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpResponse.*
import java.nio.ByteBuffer
import java.nio.file.Path
import java.time.Instant
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

class HttpDownloader(
    private val downloadPath: Path,
    private val client: HttpClient = httpClient,
) : Downloader, ComponentStateful {

    private val progresses: MutableMap<Path, Downloading> = ConcurrentHashMap()

    override fun submit(task: DownloadTask) {
        // TODO 改并发下载
        for (file in task.downloadFiles) {
            if (file.fileUri == null) {
                log.info("File uri is null, skip download $file")
                continue
            }
            val bodyHandler = MonitorableBodyHandler(BodyHandlers.ofFile(file.path))
            val cf = CompletableFuture<HttpResponse<Path>>()
            val downloading = Downloading(file, bodyHandler, cf)
            progresses.compute(file.path) { _, oldValue ->
                oldValue?.run {
                    throw IllegalStateException("File already downloading: ${file.path}")
                }
                downloading
            }
            val request = HttpRequest.newBuilder(file.fileUri).GET()
                .apply {
                    task.options.headers.forEach(this::setHeader)
                }
                .build()
            val future = client.sendAsync(request, bodyHandler).whenComplete { f, t ->
                val path = f.body()
                progresses.remove(path)
                if (t != null) {
                    log.error("Download failed: $f", t)
                } else {
                    log.info("Download success: $f")
                }
            }.join()
            downloading.future.complete(future)
        }
    }

    override fun defaultDownloadPath(): Path {
        return downloadPath
    }

    override fun cancel(sourceItem: SourceItem, files: List<SourceFile>) {
        files.forEach {
            runCatching {
                progresses[it.path]?.future?.cancel(true)
            }
            progresses.remove(it.path)
        }
    }

    companion object {

        private val log = LoggerFactory.getLogger(HttpDownloader::class.java)
    }

    override fun stateDetail(): Any {
        return progresses.map {
            val dl = it.value
            mapOf(
                "file" to dl.file.path.toString(),
                "speed" to dl.bodyHandler.speedOfRate()
            )
        }
    }
}

private data class Downloading(
    val file: SourceFile,
    val bodyHandler: MonitorableBodyHandler<*>,
    val future: CompletableFuture<HttpResponse<Path>>
)

class MonitorableBodyHandler<T>(
    private val bodyHandler: BodyHandler<T>
) : BodyHandler<T> by bodyHandler {

    private lateinit var monitor: MonitorableBodySubscriber<T>

    override fun apply(responseInfo: ResponseInfo): BodySubscriber<T> {
        val bodySubscriber = MonitorableBodySubscriber(bodyHandler.apply(responseInfo))
        monitor = bodySubscriber
        return bodySubscriber
    }

    fun speedOfRate(): String {
        return monitor.speedOfRate()
    }
}

class MonitorableBodySubscriber<T>(
    private val delegate: BodySubscriber<T>
) : BodySubscriber<T> by delegate {

    private val startTime by lazy {
        Instant.now().epochSecond
    }
    private var downloadedSize: Long = 0

    override fun onNext(item: List<ByteBuffer>) {
        downloadedSize += item.size.toLong()
        delegate.onNext(item)
    }

    fun speedOfRate(): String {
        val current = Instant.now().epochSecond
        val rate = if (current == startTime) {
            downloadedSize
        } else {
            downloadedSize / (current - startTime)
        }
        return rate.readableRate
    }

}