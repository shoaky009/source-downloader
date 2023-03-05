package xyz.shoaky.sourcedownloader.sdk.component

import xyz.shoaky.sourcedownloader.sdk.DownloadTask
import xyz.shoaky.sourcedownloader.sdk.SourceItem

/**
 * 异步下载，submit方法不会等待下载执行完毕才返回
 * 下载器提交任务成功后会有持久化重命名任务
 */
interface AsyncDownloader : Downloader {

    fun isFinished(task: DownloadTask): Boolean

    fun parseTorrentHash(sourceItem: SourceItem): String? {
        val find = regex.find(sourceItem.downloadUrl.toString())
            ?: regex.find(sourceItem.link.toString()) ?: regex.find(sourceItem.title)
        return find?.value
    }

    companion object {
        private val regex = Regex("[0-9a-f]{40}")
    }

}

interface TorrentDownloader : AsyncDownloader, FileMover