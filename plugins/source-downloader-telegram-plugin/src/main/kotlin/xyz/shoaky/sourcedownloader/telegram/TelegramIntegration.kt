package xyz.shoaky.sourcedownloader.telegram

import it.tdlight.client.SimpleTelegramClient
import it.tdlight.jni.TdApi
import xyz.shoaky.sourcedownloader.sdk.SourceFile
import xyz.shoaky.sourcedownloader.sdk.SourceItem
import xyz.shoaky.sourcedownloader.sdk.SourceItemGroup
import xyz.shoaky.sourcedownloader.sdk.component.ItemFileResolver
import xyz.shoaky.sourcedownloader.sdk.component.VariableProvider
import xyz.shoaky.sourcedownloader.sdk.util.queryMap
import kotlin.io.path.Path

class TelegramIntegration(
    private val client: SimpleTelegramClient
) : VariableProvider, ItemFileResolver {
    override fun createSourceGroup(sourceItem: SourceItem): SourceItemGroup {
        val queryMap = sourceItem.link.queryMap()
        val chatId = queryMap["chatId"]?.toLong() ?: return SourceItemGroup.EMPTY
        val messageId = queryMap["messageId"]?.toLong() ?: return SourceItemGroup.EMPTY

        val chatResultHandler = BlockingResultHandler<TdApi.Chat>()
        client.send(TdApi.GetChat(), chatResultHandler)
        val title = chatResultHandler.get().title

        val type = sourceItem.contentType.split("/").first()
        return TelegramItemGroup(TelegramVariable(chatId, messageId, title, type))
    }

    override fun support(item: SourceItem): Boolean = item.downloadUri.scheme == "telegram"
    override fun resolveFiles(sourceItem: SourceItem): List<SourceFile> {
        val queryMap = sourceItem.downloadUri.queryMap()
        // 最稳定的是从messageId获取fileId,从uri获取重启应用后fileId会变导致下载到错误的文件
        val fileId = queryMap["fileId"]?.toInt() ?: return emptyList()

        val blockingResultHandler = BlockingResultHandler<TdApi.Text>()
        client.send(TdApi.GetSuggestedFileName(fileId, "src/test/resources/downloads"), blockingResultHandler)
        val text = blockingResultHandler.future.get().get().text

        return listOf(SourceFile(Path(text)))
    }
}

