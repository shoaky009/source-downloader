package io.github.shoaky.sourcedownloader.telegram

import io.github.shoaky.sourcedownloader.sdk.*
import io.github.shoaky.sourcedownloader.sdk.component.ComponentStateful
import io.github.shoaky.sourcedownloader.sdk.component.Downloader
import io.github.shoaky.sourcedownloader.sdk.component.ItemFileResolver
import io.github.shoaky.sourcedownloader.sdk.component.VariableProvider
import io.github.shoaky.sourcedownloader.sdk.util.queryMap
import reactor.core.scheduler.Schedulers
import telegram4j.core.MTProtoTelegramClient
import telegram4j.core.`object`.Document
import telegram4j.core.`object`.MessageMedia
import telegram4j.core.`object`.Photo
import telegram4j.core.`object`.media.PhotoThumbnail
import telegram4j.tl.ImmutableInputMessageID
import java.nio.ByteBuffer
import java.nio.channels.ByteChannel
import java.nio.channels.FileChannel
import java.nio.channels.SeekableByteChannel
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.text.NumberFormat
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.*
import kotlin.jvm.optionals.getOrDefault
import kotlin.jvm.optionals.getOrNull

/**
 * Telegram集成，提供下载、解析文件、变量提供
 */
class TelegramIntegration(
    private val client: MTProtoTelegramClient,
    private val downloadPath: Path
) : VariableProvider, ItemFileResolver, Downloader, ComponentStateful, AutoCloseable {

    private val progresses: MutableMap<Path, ProgressiveChannel> = ConcurrentHashMap()
    private val downloadCounting = AtomicInteger(0)
    private val hashingPathMapping = ConcurrentHashMap<String, Path>()

    override fun createSourceGroup(sourceItem: SourceItem): SourceItemGroup {
        val queryMap = sourceItem.link.queryMap()
        val chatId = queryMap["chatId"]?.toLong()
        val messageId = queryMap["messageId"]?.toInt()

        val chat = chatId?.let {
            client.getChatMinById(ChatPointer(it).createId())
                .blockOptional().getOrNull()
        }

        val messageVariable = MessageVariable(
            chat?.id?.asLong(),
            messageId,
            chat?.name
        )
        return object : SourceItemGroup {

            override fun filePatternVariables(paths: List<SourceFile>): List<FileVariable> {
                return paths.map { FileVariable.EMPTY }
            }

            override fun sharedPatternVariables(): PatternVariables {
                return messageVariable
            }
        }
    }

    override fun support(item: SourceItem): Boolean = item.downloadUri.scheme == "telegram"

    override fun resolveFiles(sourceItem: SourceItem): List<SourceFile> {
        val sourceFile = SourceFile(Path(sourceItem.title), sourceItem.attrs)
        return listOf(sourceFile)
    }

    override fun submit(task: DownloadTask) {
        val queryMap = task.downloadUri().queryMap()
        val chatId = queryMap["chatId"]?.toLong()
        val messageId = queryMap["messageId"]?.toInt()
        if (chatId == null || messageId == null) {
            log.error("Invalid download uri: ${task.downloadUri()}")
            return
        }

        val chatPointer = ChatPointer(chatId)
        val documentOp = client.getMessages(
            chatPointer.createId(), listOf(
            ImmutableInputMessageID.of(messageId)
        ))
            .mapNotNull { it.messages.firstOrNull()?.media?.getOrNull() as? MessageMedia.Document }
            .mapNotNull { it?.document?.get() }
            .blockOptional(Duration.ofSeconds(5L))

        if (!documentOp.isPresent) {
            log.warn("SourceItem document not found: ${task.sourceItem}")
            return
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

        client.downloadFile(document.fileReferenceId)
            .doFirst {
                log.info("Start downloading file: $fileDownloadPath")
            }
            .publishOn(Schedulers.boundedElastic())
            .collect({ monitoredChannel }, { fc, filePart ->
                fc.write(filePart.bytes.nioBuffer())
            })
            .doFinally {
                runCatching {
                    monitoredChannel.close()
                }.onFailure {
                    log.error("Error closing file channel", it)
                }
                progresses.remove(fileDownloadPath)
                hashingPathMapping[hashing] = fileDownloadPath
            }
            .doOnSuccess {
                downloadCounting.incrementAndGet()
                tempDownloadPath.moveTo(fileDownloadPath)
                log.info("Downloaded file: $fileDownloadPath")
            }
            .doOnError {
                log.error("Error downloading file", it)
            }
            .block()
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

private data class MessageVariable(
    val chatId: Long?,
    val messageId: Int?,
    val chatName: String?,
) : PatternVariables

class ProgressiveChannel(
    private val totalSize: Long,
    private val ch: SeekableByteChannel
) : ByteChannel by ch {

    private var downloadedBytes = 0L
    private val startTime = Instant.now()

    override fun write(src: ByteBuffer): Int {
        val write = ch.write(src)
        downloadedBytes += write
        return write
    }

    fun formatProgress(): String {
        return NumberFormat.getPercentInstance().format(downloadedBytes.toDouble() / totalSize.toDouble())
    }

    fun formatRate(): String {
        val curr = Instant.now().epochSecond
        val rate = if (curr == startTime.epochSecond) {
            downloadedBytes
        } else {
            downloadedBytes / (curr - startTime.epochSecond)
        }

        return when {
            rate > GIGABYTE -> {
                String.format("%.2f GiB/s", rate / GIGABYTE)
            }

            rate > MEGABYTE -> {
                String.format("%.2f MiB/s", rate / MEGABYTE)
            }

            rate > KILOBYTE -> {
                String.format("%.2f KiB/s", rate / KILOBYTE)
            }

            else -> {
                "$rate B/s"
            }
        }
    }

    fun formatTotalSize(): String {
        return when {
            totalSize > GIGABYTE -> {
                String.format("%.2f GiB", totalSize / GIGABYTE)
            }

            totalSize > MEGABYTE -> {
                String.format("%.2f MiB", totalSize / MEGABYTE)
            }

            totalSize > KILOBYTE -> {
                String.format("%.2f KiB", totalSize / KILOBYTE)
            }

            else -> {
                "$totalSize B"
            }
        }
    }

    fun getDuration(): Long {
        return Duration.ofSeconds(Instant.now().epochSecond - startTime.epochSecond).seconds
    }

    override fun close() {
        ch.close()
    }

    companion object {

        private const val KILOBYTE = 1024.0
        private const val MEGABYTE = KILOBYTE * 1024.0
        private const val GIGABYTE = MEGABYTE * 1024.0
    }
}