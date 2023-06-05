package xyz.shoaky.sourcedownloader.telegram.other

import reactor.core.scheduler.Schedulers
import telegram4j.core.MTProtoTelegramClient
import telegram4j.core.`object`.MessageMedia
import telegram4j.tl.ImmutableInputMessageID
import xyz.shoaky.sourcedownloader.sdk.*
import xyz.shoaky.sourcedownloader.sdk.component.Downloader
import xyz.shoaky.sourcedownloader.sdk.component.ItemFileResolver
import xyz.shoaky.sourcedownloader.sdk.component.VariableProvider
import xyz.shoaky.sourcedownloader.sdk.util.queryMap
import java.nio.ByteBuffer
import java.nio.channels.ByteChannel
import java.nio.channels.FileChannel
import java.nio.channels.SeekableByteChannel
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.text.NumberFormat
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.fixedRateTimer
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.moveTo
import kotlin.io.path.name
import kotlin.jvm.optionals.getOrNull

class TelegramIntegration(
    private val client: MTProtoTelegramClient,
    private val downloadPath: Path
) : VariableProvider, ItemFileResolver, Downloader, ComponentStateful {

    private val progresses: MutableMap<String, ProgressiveChannel> = ConcurrentHashMap()

    override fun createSourceGroup(sourceItem: SourceItem): SourceItemGroup {
        val queryMap = sourceItem.link.queryMap()
        val chatId = queryMap["chatId"]?.toLong()
        val messageId = queryMap["messageId"]?.toInt()

        val chat = chatId?.let {
            client.getChatMinById(ChatPointer(it, 0).createId())
                .blockOptional().getOrNull()
        }

        val messageVariable = MessageVariable(
            chatId,
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
        if (sourceItem.contentType == "image/jpg" && sourceItem.title.lastIndexOf(".") < 0) {
            return listOf(
                SourceFile(Path("${sourceItem.title}.jpg"), mapOf("telegramDocumentType" to "photo"))
            )
        }
        return listOf(
            SourceFile(Path(sourceItem.title))
        )
    }

    override fun submit(task: DownloadTask) {
        val queryMap = task.downloadUri().queryMap()
        val chatId = queryMap["chatId"]?.toLong()
        val messageId = queryMap["messageId"]?.toInt()
        if (chatId == null || messageId == null) {
            log.error("Invalid download uri: ${task.downloadUri()}")
            return
        }
        val chatPointer = ChatPointer(chatId, 0)
        val documentOp = client.getMessages(
            chatPointer.createId(), listOf(
            ImmutableInputMessageID.of(messageId)
        )
        )
            .mapNotNull { it.messages.firstOrNull()?.media?.getOrNull() as? MessageMedia.Document }
            .mapNotNull { it?.document?.get() }
            .blockOptional()

        if (!documentOp.isPresent) {
            log.warn("SourceItem document not found: ${task.sourceItem}")
            return
        }
        val fileDownloadPath = task.downloadFiles.first()
        fileDownloadPath.parent.createDirectories()
        val document = documentOp.get()

        val tempDownloadPath = fileDownloadPath.resolveSibling("${fileDownloadPath.name}.tmp")
        val monitoredChannel = ProgressiveChannel(
            document.size.get(),
            FileChannel.open(
                tempDownloadPath,
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE,
            )
        )

        progresses[fileDownloadPath.name] = monitoredChannel
        client.downloadFile(document.fileReferenceId)
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
                progresses.remove(fileDownloadPath.name)
                tempDownloadPath.moveTo(fileDownloadPath)
            }
            .doOnError {
                log.error("Error downloading file", it)
            }
            .block()
    }

    override fun defaultDownloadPath(): Path {
        return downloadPath
    }

    override fun stateDetail(): Any {
        return progresses.map {
            val channel = it.value
            mapOf(
                "path" to it.key,
                "totalSize" to channel.formatTotalSize(),
                "progress" to channel.formatProgress(),
                "rate" to channel.formatRate(),
                "duration" to channel.getDuration()
            )
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

    private var current = 0L
    private var rate = .0
    private var previousSize = 0L
    private val startTime = System.currentTimeMillis()

    private val timer = fixedRateTimer(initialDelay = 1000, period = 1000) {
        rate = (current - previousSize) / 1000.0
        previousSize = current
    }

    override fun write(src: ByteBuffer): Int {
        val write = ch.write(src)
        current += write
        return write
    }

    fun formatProgress(): String {
        return NumberFormat.getPercentInstance().format(current.toDouble() / totalSize.toDouble())
    }

    fun formatRate(): String {
        return when {
            rate > gigabyte -> {
                "${rate / gigabyte} GB/s"
            }

            rate > megabyte -> {
                "${rate / megabyte} MB/s"
            }

            rate > kilobyte -> {
                "${rate / kilobyte} KB/s"
            }

            else -> {
                "$rate B/s"
            }
        }
    }

    fun formatTotalSize(): String {
        return when {
            totalSize > gigabyte -> {
                "${totalSize / gigabyte} GB"
            }

            totalSize > megabyte -> {
                "${totalSize / megabyte} MB"
            }

            totalSize > kilobyte -> {
                "${totalSize / kilobyte} KB"
            }

            else -> {
                "$totalSize B"
            }
        }
    }

    fun getDuration(): Long {
        return Duration.ofMillis(System.currentTimeMillis() - startTime).seconds
    }

    override fun close() {
        runCatching {
            timer.cancel()
        }
        ch.close()
    }

    companion object {
        private const val kilobyte = 1024
        private const val megabyte = kilobyte * 1024
        private const val gigabyte = megabyte * 1024
    }
}