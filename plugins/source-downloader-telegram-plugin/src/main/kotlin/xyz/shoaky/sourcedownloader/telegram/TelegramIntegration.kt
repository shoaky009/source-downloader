package xyz.shoaky.sourcedownloader.telegram

import it.tdlight.client.SimpleTelegramClient
import it.tdlight.jni.TdApi
import org.slf4j.LoggerFactory
import xyz.shoaky.sourcedownloader.sdk.*
import xyz.shoaky.sourcedownloader.sdk.component.Downloader
import xyz.shoaky.sourcedownloader.sdk.component.ItemFileResolver
import xyz.shoaky.sourcedownloader.sdk.component.VariableProvider
import xyz.shoaky.sourcedownloader.sdk.util.queryMap
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.moveTo

class TelegramIntegration(
    private val client: SimpleTelegramClient,
    private val downloadPath: Path
) : VariableProvider, ItemFileResolver, Downloader {
    override fun createSourceGroup(sourceItem: SourceItem): SourceItemGroup {
        val queryMap = sourceItem.link.queryMap()
        val chatId = queryMap["chatId"]?.toLong() ?: throw IllegalArgumentException("chatId is null")
        val messageId = queryMap["messageId"]?.toInt()

        val chatResultHandler = BlockingResultHandler<TdApi.Chat>()
        client.send(TdApi.GetChat(chatId), chatResultHandler)
        val chat = chatResultHandler.get()

        val messageVariable = MessageVariable(
            chat.id,
            messageId,
            chat.title
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
        val uri = task.downloadUri()
        val queryMap = uri.queryMap()
        // 最稳定的是从messageId获取fileId,从uri获取重启应用后fileId会变导致下载到错误的文件
        val fileId = queryMap["fileId"]?.toInt() ?: return

        val fileDownloadPath = task.downloadFiles.first()
        val downloadFileHandler = BlockingResultHandler<TdApi.File>(-1)

        log.info("Start downloading file: $fileDownloadPath")
        client.send(TdApi.DownloadFile(fileId, 1, 0, 0, true), downloadFileHandler)
        log.info("Downloaded file: $fileDownloadPath")

        val path = Path(downloadFileHandler.get().local.path)
        if (path != fileDownloadPath) {
            path.moveTo(fileDownloadPath)
        }
    }

    override fun defaultDownloadPath(): Path {
        return downloadPath
    }

    companion object {
        private val log = LoggerFactory.getLogger(TelegramIntegration::class.java)
    }
}


private data class MessageVariable(
    val chatId: Long?,
    val messageId: Int?,
    val chatName: String?,
) : PatternVariables