package xyz.shoaky.sourcedownloader.telegram

import it.tdlight.client.SimpleTelegramClient
import it.tdlight.jni.TdApi.DownloadFile
import it.tdlight.jni.TdApi.File
import xyz.shoaky.sourcedownloader.sdk.DownloadTask
import xyz.shoaky.sourcedownloader.sdk.component.Downloader
import xyz.shoaky.sourcedownloader.sdk.util.queryMap
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.moveTo

class TelegramDownloader(
    private val client: SimpleTelegramClient,
    private val downloadPath: Path
) : Downloader {
    override fun submit(task: DownloadTask) {
        val uri = task.downloadUri()
        val queryMap = uri.queryMap()
        // 最稳定的是从messageId获取fileId,从uri获取重启应用后fileId会变导致下载到错误的文件
        val fileId = queryMap["fileId"]?.toInt() ?: return

        val downloadFile = task.downloadFiles.first()
        val filePartHandler = BlockingResultHandler<File>()
        client.send(DownloadFile(fileId, 1, 0, 0, true), filePartHandler)

        // ReadFilePart
        val path = filePartHandler.get().local.path
        Path(path).moveTo(downloadFile)
    }

    override fun defaultDownloadPath(): Path {
        return downloadPath
    }
}