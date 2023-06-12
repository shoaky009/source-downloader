package io.github.shoaky.sourcedownloader.sdk.component

import io.github.shoaky.sourcedownloader.sdk.DownloadTask
import io.github.shoaky.sourcedownloader.sdk.SourceItem

/**
 * 异步下载，submit方法不会等待下载执行完毕才返回
 * 下载器提交任务成功后会有持久化重命名任务
 */
interface AsyncDownloader : Downloader {

    /**
     * @return null当下载器不存在该任务时
     */
    fun isFinished(task: DownloadTask): Boolean?

}

interface TorrentDownloader : AsyncDownloader, FileMover {

    companion object {
        private val torrentHashRegex = Regex("[0-9a-f]{40}")
        fun tryParseTorrentHash(sourceItem: SourceItem): String? {
            val find = torrentHashRegex.find(sourceItem.downloadUri.toString())
                ?: torrentHashRegex.find(sourceItem.link.toString()) ?: torrentHashRegex.find(sourceItem.title)
            return find?.value
        }
    }
}