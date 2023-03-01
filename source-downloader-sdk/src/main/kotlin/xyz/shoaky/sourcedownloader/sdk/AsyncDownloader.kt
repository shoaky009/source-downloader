package xyz.shoaky.sourcedownloader.sdk

import xyz.shoaky.sourcedownloader.sdk.component.Downloader
import xyz.shoaky.sourcedownloader.sdk.component.FileMover

/**
 * 异步下载，submit方法不会等待下载执行完毕才返回
 * 下载器提交任务成功后会有持久化重命名任务
 */
interface AsyncDownloader : Downloader {

    fun isFinished(task: DownloadTask): Boolean
}

interface TorrentDownloader : AsyncDownloader, FileMover