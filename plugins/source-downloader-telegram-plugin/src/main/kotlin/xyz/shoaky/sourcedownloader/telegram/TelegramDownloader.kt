package xyz.shoaky.sourcedownloader.telegram

import it.tdlight.client.SimpleTelegramClient
import it.tdlight.jni.TdApi
import it.tdlight.jni.TdApi.AddFileToDownloads
import it.tdlight.jni.TdApi.DownloadFile
import xyz.shoaky.sourcedownloader.sdk.DownloadTask
import xyz.shoaky.sourcedownloader.sdk.component.Downloader
import java.nio.file.Path

class TelegramDownloader(
    private val client: SimpleTelegramClient,
    private val downloadPath: Path
) : Downloader {
    override fun submit(task: DownloadTask) {
        val uri = task.downloadUri()
        // CancelDownloadFile
        // DownloadFile
        // AddFileToDownloads
        val downloadFile = DownloadFile()
        client.send(TdApi.AddFileToDownloads()){
            it.get().local.path
        }
        TODO("Not yet implemented")
    }

    override fun defaultDownloadPath(): Path {
        return downloadPath
    }
}