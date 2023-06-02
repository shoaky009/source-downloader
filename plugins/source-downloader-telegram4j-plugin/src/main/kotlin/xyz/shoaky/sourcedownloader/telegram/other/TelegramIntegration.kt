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
import java.nio.channels.FileChannel
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.jvm.optionals.getOrNull

class TelegramIntegration(
    private val client: MTProtoTelegramClient,
    private val downloadPath: Path
) : VariableProvider, ItemFileResolver, Downloader {

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
        val documentOp = client.getMessages(chatPointer.createId(), listOf(
            ImmutableInputMessageID.of(messageId)
        ))
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

        val fileChannel = FileChannel.open(
            fileDownloadPath,
            StandardOpenOption.WRITE,
            StandardOpenOption.CREATE,
        )
        client.downloadFile(document.fileReferenceId)
            .publishOn(Schedulers.boundedElastic())
            .collect({ fileChannel }, { fc, filePart ->
                fc.write(filePart.bytes.nioBuffer())
            })
            .doFinally {
                fileChannel.close()
            }.block()
    }

    override fun defaultDownloadPath(): Path {
        return downloadPath
    }
}

private data class MessageVariable(
    val chatId: Long?,
    val messageId: Int?,
    val chatName: String?,
) : PatternVariables
