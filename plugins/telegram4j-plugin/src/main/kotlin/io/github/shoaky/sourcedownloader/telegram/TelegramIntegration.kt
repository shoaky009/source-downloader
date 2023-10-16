package io.github.shoaky.sourcedownloader.telegram

import io.github.shoaky.sourcedownloader.sdk.DownloadTask
import io.github.shoaky.sourcedownloader.sdk.ProcessingException
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.ComponentStateful
import io.github.shoaky.sourcedownloader.sdk.component.Downloader
import io.github.shoaky.sourcedownloader.sdk.component.ItemFileResolver
import io.github.shoaky.sourcedownloader.sdk.util.queryMap
import io.github.shoaky.sourcedownloader.telegram.util.ProgressiveChannel
import reactor.core.scheduler.Schedulers
import telegram4j.core.`object`.Document
import telegram4j.core.`object`.MessageMedia
import telegram4j.core.`object`.Photo
import telegram4j.core.`object`.media.PhotoThumbnail
import telegram4j.mtproto.RpcException
import telegram4j.tl.ImmutableInputMessageID
import java.nio.channels.FileChannel
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.*
import kotlin.jvm.optionals.getOrDefault
import kotlin.jvm.optionals.getOrNull

/**
 * Telegram集成，提供下载、解析文件、变量提供
 */
class TelegramIntegration(
    wrapper: TelegramClientWrapper,
    private val downloadPath: Path
) : ItemFileResolver, Downloader, ComponentStateful, AutoCloseable {

    private val client = wrapper.client
    private val progresses: MutableMap<Path, ProgressiveChannel> = ConcurrentHashMap()
    private val downloadCounting = AtomicInteger(0)
    private val hashingPathMapping = ConcurrentHashMap<String, Path>()

    override fun resolveFiles(sourceItem: SourceItem): List<SourceFile> {
        val sourceFile = SourceFile(Path(sourceItem.title), sourceItem.attrs)
        return listOf(sourceFile)
    }

    override fun submit(task: DownloadTask): Boolean {
        val queryMap = task.downloadUri().queryMap()
        val chatId = queryMap["chatId"]?.toLong()
        val messageId = queryMap["messageId"]?.toInt()
        if (chatId == null || messageId == null) {
            log.error("Invalid download uri: ${task.downloadUri()}")
            return false
        }

        val chatPointer = ChatPointer(chatId)
        val documentOp = client.getMessages(
            chatPointer.createId(), listOf(
                ImmutableInputMessageID.of(messageId)
            )
        )
            .mapNotNull { it.messages.firstOrNull()?.media?.getOrNull() as? MessageMedia.Document }
            .mapNotNull { it?.document?.get() }
            .blockOptional(Duration.ofSeconds(5L))

        if (!documentOp.isPresent) {
            log.warn("SourceItem document not found: ${task.sourceItem}")
            return false
        }
        val fileDownloadPath = task.downloadFiles.map { it.path }.first()
        fileDownloadPath.parent.createDirectories()
        val document = documentOp.get()

        val tempDownloadPath = fileDownloadPath.resolveSibling("${fileDownloadPath.name}.tmp")
        val monitoredChannel = ProgressiveChannel(
            getSize(document),
            FileChannel.open(
                tempDownloadPath,
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE,
            )
        )

        progresses.compute(fileDownloadPath) { _, oldValue ->
            oldValue?.run {
                throw IllegalStateException("File already downloading: $fileDownloadPath")
            }
            monitoredChannel
        }
        val hashing = task.sourceItem.hashing()
        hashingPathMapping[hashing] = fileDownloadPath

        val refreshedFileReferenceId = client.refresh(document.fileReferenceId)
            .blockOptional(Duration.ofSeconds(5L)).get()
        client.downloadFile(refreshedFileReferenceId)
            .doFirst {
                log.info("Start downloading file: $fileDownloadPath")
            }
            .publishOn(Schedulers.boundedElastic())
            // 该设置临时解决没有发射filePart的问题，只有在网络不好的情况下才会出现，暂时没排查出来Telegram4j这个库哪里的问题
            // 缺点是会抛出异常，下一次再重新下载
            .timeout(Duration.ofMinutes(2))
            .collect({ monitoredChannel }, { fc, filePart ->
                fc.write(filePart.bytes.nioBuffer())
            })
            .doOnSuccess {
                downloadCounting.incrementAndGet()
                tempDownloadPath.moveTo(fileDownloadPath)
                log.info("Downloaded file: $fileDownloadPath")
            }
            .doOnError {
                log.error("Error downloading file", it)
            }
            .onErrorMap {
                wrapRetryableExceptionIfNeeded(it)
            }
            .doFinally {
                runCatching {
                    monitoredChannel.close()
                }.onFailure {
                    log.error("Error closing file channel", it)
                }
                progresses.remove(fileDownloadPath)
                hashingPathMapping[hashing] = fileDownloadPath
            }
            .block()
        return true
    }

    private fun getSize(document: Document): Long {
        if (document is Photo) {
            return document.thumbs.getOrDefault(emptyList())
                .filterIsInstance<PhotoThumbnail>().maxOfOrNull {
                    it.size.toLong()
                } ?: 0L
        }
        return document.size.getOrDefault(0)
    }

    override fun defaultDownloadPath(): Path {
        return downloadPath
    }

    /**
     * Not tested
     */
    override fun cancel(sourceItem: SourceItem, files: List<SourceFile>) {
        val path = hashingPathMapping[sourceItem.hashing()]
        val progressiveChannel = progresses[path] ?: return
        progressiveChannel.close()
    }

    override fun stateDetail(): Any {
        return mapOf(
            "downloaded" to downloadCounting.get(),
            "downloading" to progresses.map {
                val channel = it.value
                mapOf(
                    "path" to it.key.toString(),
                    "totalSize" to channel.formatTotalSize(),
                    "progress" to channel.formatProgress(),
                    "rate" to channel.formatRate(),
                    "duration" to channel.getDuration()
                )
            },
        )
    }

    override fun close() {
        progresses.forEach { (t, u) ->
            runCatching {
                u.close()
                t.resolveSibling("${t.name}.tmp").deleteIfExists()
            }.onFailure {
                log.error("Error closing downloader", it)
            }
        }
    }
}

fun wrapRetryableExceptionIfNeeded(e: Throwable): Throwable {
    if (e is RpcException) {
        if (e.error.errorMessage().contains("FLOOD_WAIT")) {
            return ProcessingException.retryable(e.error.errorMessage(), e)
        }
    }
    return e
}