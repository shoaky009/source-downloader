package io.github.shoaky.sourcedownloader.component.downloader

import io.github.shoaky.sourcedownloader.sdk.DownloadTask
import io.github.shoaky.sourcedownloader.sdk.ProcessingException
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.ComponentStateful
import io.github.shoaky.sourcedownloader.sdk.component.Downloader
import io.github.shoaky.sourcedownloader.sdk.http.StatusCodes
import io.github.shoaky.sourcedownloader.sdk.util.http.httpClient
import io.github.shoaky.sourcedownloader.sdk.util.readableRate
import kotlinx.coroutines.*
import kotlinx.coroutines.future.asDeferred
import org.slf4j.LoggerFactory
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.*
import java.nio.ByteBuffer
import java.nio.file.Path
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.notExists

/**
 * HTTP下载器，URI取自[SourceFile.downloadUri]
 */
class HttpDownloader(
    private val downloadPath: Path,
    private val client: HttpClient = httpClient,
    parallelism: Int = 5
) : Downloader, ComponentStateful {

    private val progresses: MutableMap<Path, Downloading> = ConcurrentHashMap()

    private val dispatcher: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(parallelism)

    override fun submit(task: DownloadTask): Boolean {
        runBlocking(dispatcher) {
            task.downloadFiles.forEach {
                launch {
                    downloadSourceFile(it, task.options.headers)
                }
            }
        }
        return true
    }

    private suspend fun downloadSourceFile(file: SourceFile, headers: Map<String, String>) {
        val path = file.path
        if (progresses.containsKey(path)) {
            throw IllegalStateException("File already downloading: $path")
        }
        if (file.downloadUri == null) {
            log.info("Skip download: $path case fileUri is null")
            return
        }

        if (path.parent != downloadPath && path.parent.notExists()) {
            path.parent.createDirectories()
        }
        val bodyHandler = MonitorableBodyHandler(BodyHandlers.ofFile(path))
        val request = HttpRequest.newBuilder(file.downloadUri).GET()
            .apply {
                headers.forEach(this::setHeader)
            }
            .build()

        val responseDef = client.sendAsync(request, bodyHandler).asDeferred()
        responseDef.invokeOnCompletion {
            progresses.remove(path)
            if (it != null) {
                log.error("Download failed: $path", it)
                path.deleteIfExists()
            } else {
                log.info("Download completed: $path")
            }
        }
        progresses[path] = Downloading(file, bodyHandler, responseDef.job)
        val response = responseDef.await()
        val statusCode = response.statusCode()
        if (statusCode == StatusCodes.NOT_FOUND) {
            throw ProcessingException.skip("Failed to download status code: ${response.statusCode()} $path, uri:${file.downloadUri}")
        }
        if (statusCode >= 400) {
            throw IllegalStateException("Failed to download status code: ${response.statusCode()} $path")
        }
    }

    override fun defaultDownloadPath(): Path {
        return downloadPath
    }

    override fun cancel(sourceItem: SourceItem, files: List<SourceFile>) {
        files.forEach {
            runCatching {
                progresses[it.path]?.job?.cancel("Cancel by item: $sourceItem")
            }
            progresses.remove(it.path)
        }
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

    companion object {

        private val log = LoggerFactory.getLogger(HttpDownloader::class.java)
    }
}

private data class Downloading(
    val file: SourceFile,
    val bodyHandler: MonitorableBodyHandler<*>,
    val job: Job
)

class MonitorableBodyHandler<T>(
    private val bodyHandler: BodyHandler<T>
) : BodyHandler<T> by bodyHandler {

    private var monitor: MonitorableBodySubscriber<T>? = null

    override fun apply(responseInfo: ResponseInfo): BodySubscriber<T> {
        val bodySubscriber = MonitorableBodySubscriber(bodyHandler.apply(responseInfo))
        monitor = bodySubscriber
        return bodySubscriber
    }

    fun speedOfRate(): String {
        return monitor?.speedOfRate() ?: "0B/s"
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
        downloadedSize += item.sumOf { it.remaining() }
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