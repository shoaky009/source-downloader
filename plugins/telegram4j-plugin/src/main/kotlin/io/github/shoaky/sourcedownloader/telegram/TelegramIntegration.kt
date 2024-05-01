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
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import telegram4j.core.`object`.Document
import telegram4j.core.`object`.MessageMedia
import telegram4j.core.`object`.Photo
import telegram4j.core.`object`.Video
import telegram4j.core.`object`.media.PhotoThumbnail
import telegram4j.core.util.Id
import telegram4j.mtproto.MTProtoRetrySpec
import telegram4j.mtproto.RpcException
import telegram4j.mtproto.file.FilePart
import telegram4j.mtproto.file.FileReferenceId
import telegram4j.tl.ImmutableInputMessageID
import telegram4j.tl.mtproto.RpcError
import java.nio.channels.FileChannel
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.*
import kotlin.jvm.optionals.getOrDefault
import kotlin.jvm.optionals.getOrNull

/**
 * Telegram集成，提供下载、解析文件、变量提供
 */
class TelegramIntegration(
    wrapper: TelegramClientWrapper,
    private val downloadPath: Path,
) : ItemFileResolver, Downloader, ComponentStateful, AutoCloseable {

    private val client = wrapper.client

    /**
     * Key is the path of the file downloading.
     */
    private val progresses: MutableMap<Path, ProgressiveChannel> = ConcurrentHashMap()
    private val downloadedCounting = AtomicInteger(0)
    private val hashingPathMapping = ConcurrentHashMap<String, Path>()

    override fun resolveFiles(sourceItem: SourceItem): List<SourceFile> {
        if (sourceItem.contentType == "message") {
            return resolveFromMessage(sourceItem)
        }

        if (sourceItem.getAttr<String>("site") == "Telegraph") {
            return emptyList()
        }

        val sourceFile = SourceFile(Path(sourceItem.title))
        return listOf(sourceFile)
    }

    private fun resolveFromMessage(sourceItem: SourceItem): List<SourceFile> {
        val queryMap = sourceItem.downloadUri.queryMap()
        val chatId = queryMap["channel"]?.toLong() ?: return emptyList()
        val messageId = queryMap["post"]?.toInt() ?: return emptyList()

        val messageIdPeer = listOf(ImmutableInputMessageID.of(messageId))
        val chatIdPeer = Id.ofChannel(chatId)
        val message = client.getMessages(chatIdPeer, messageIdPeer)
            .retryWhen(MTProtoRetrySpec.max(2))
            .block(Duration.ofSeconds(5L))
            ?.messages?.firstOrNull() ?: return emptyList()

        val styledContent = TextStyleSupport.styled(message.content, message.entities)
        return listOf(
            SourceFile(
                Path("${sourceItem.title}.md"),
                data = styledContent.byteInputStream()
            )
        )
    }

    override fun submit(task: DownloadTask): Boolean {
        val queryMap = task.downloadUri().queryMap()
        val chatId = queryMap["channel"]?.toLong()
        val messageId = queryMap["post"]?.toInt()
        if (chatId == null || messageId == null) {
            log.error("Invalid download uri: ${task.downloadUri()}")
            return false
        }

        val chatPointer = ChatPointer(chatId)
        val documentOp = client.getMessages(
            chatPointer.createId(), listOf(
                ImmutableInputMessageID.of(messageId)
            )
        ).retryWhen(MTProtoRetrySpec.max(2))
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
            ),
        )

        // 先刷新再占用，防止网络请求失败
        val fileReferenceId = determineFileRefId(document)
        progresses.compute(fileDownloadPath) { _, oldValue ->
            oldValue?.run {
                throw IllegalStateException("File already downloading: $fileDownloadPath")
            }
            monitoredChannel
        }
        val hashing = task.sourceItem.hashing()
        hashingPathMapping[hashing] = fileDownloadPath

        log.info("Start downloading file: $fileDownloadPath")
        createFilePartStream(fileReferenceId, monitoredChannel, fileDownloadPath)
            .collect({ monitoredChannel }, { fc, filePart ->
                fc.write(filePart.bytes.nioBuffer())
            })
            .doOnSuccess {
                tempDownloadPath.moveTo(fileDownloadPath)
                downloadedCounting.incrementAndGet()
                log.info("Downloaded file: $fileDownloadPath")
            }
            .doOnError {
                log.error("Error downloading file:$fileDownloadPath", it)
                log.info(
                    "localFileSize:{} vs channelPos:{}, channelWriteTimes:{}",
                    tempDownloadPath.fileSize(),
                    monitoredChannel.getDownloadedBytes(),
                    monitoredChannel.writeTimes()
                )
            }
            .onErrorMap {
                wrapRetryableExceptionIfNeeded(it)
            }
            .doFinally {
                runCatching {
                    closePath(fileDownloadPath)
                }.onFailure {
                    log.error("Error closing file channel", it)
                }
                hashingPathMapping.remove(hashing)
            }
            .block()
        return true
    }

    private fun createFilePartStream(
        fileReferenceId: FileReferenceId,
        monitoredChannel: ProgressiveChannel,
        fileDownloadPath: Path
    ): Flux<FilePart> {
        val offset = monitoredChannel.getDownloadedBytes()
        log.info("Create file part stream for file: {}, offset:{}", fileDownloadPath, offset)
        return client.downloadFile(fileReferenceId, monitoredChannel.getDownloadedBytes(), MAX_FILE_PART_SIZE, true)
            .publishOn(Schedulers.fromExecutor(Executors.newVirtualThreadPerTaskExecutor()))
            .timeout(Duration.ofMinutes(3))
            .onErrorResume(RpcException::class.java) {
                val error = it.error
                if (monitoredChannel.isDone().not() && retryErrorCodes.contains(error.errorCode())) {
                    log.warn("Error downloading file: $fileDownloadPath, error:{}, retrying", error)
                    val delay = getDelay(it.error)
                    return@onErrorResume createFilePartStream(fileReferenceId, monitoredChannel, fileDownloadPath)
                        .delaySubscription(delay)
                }
                Flux.error(it)
            }
    }

    private fun getDelay(error: RpcError): Duration {
        if (error.errorCode() != FLOOD_WAIT_CODE) {
            return Duration.ofSeconds(DEFAULT_INTERVAL_SEC)
        }
        if (error.errorMessage().startsWith("FLOOD_WAIT_")) {
            val seconds = try {
                error.errorMessage().substringAfter("FLOOD_WAIT_").toLong() + 3
            } catch (e: NumberFormatException) {
                log.error("Error parsing FLOOD_WAIT error message: ${error.errorMessage()}", e)
                DEFAULT_INTERVAL_SEC
            }
            return Duration.ofSeconds(seconds)
        }
        return Duration.ofSeconds(DEFAULT_INTERVAL_SEC)
    }

    private fun determineFileRefId(document: Document): FileReferenceId {
        return when (document) {
            // 最清晰的
            is Video -> document.fileReferenceId.withThumbSizeType(' ')
            is Photo -> {
                client.refresh(document.fileReferenceId)
                    .blockOptional(Duration.ofSeconds(5L)).get()
            }

            else -> document.fileReferenceId
        }
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
            "downloaded" to downloadedCounting.get(),
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
        progresses.forEach { (path, _) ->
            runCatching {
                closePath(path)
            }.onFailure {
                log.error("Error closing downloader", it)
            }
        }
    }

    private fun closePath(path: Path) {
        progresses[path]?.close()
        progresses.remove(path)
        path.resolveSibling("${path.name}.tmp").deleteIfExists()
    }

    companion object {

        private const val TIMEOUT_CODE = -503
        private const val FLOOD_WAIT_CODE = 420
        private const val MAX_FILE_PART_SIZE = 1024 * 1024
        private const val DEFAULT_INTERVAL_SEC = 5L
        private val retryErrorCodes = setOf(
            TIMEOUT_CODE,
            FLOOD_WAIT_CODE
        )
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