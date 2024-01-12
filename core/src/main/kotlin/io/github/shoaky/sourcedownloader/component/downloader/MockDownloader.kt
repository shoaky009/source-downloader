package io.github.shoaky.sourcedownloader.component.downloader

import io.github.shoaky.sourcedownloader.sdk.DownloadTask
import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.TorrentDownloader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.moveTo
import kotlin.io.path.notExists

/**
 * 模拟下载器，通过创建空文件表示文件已下载完成
 */
class MockDownloader(
    private val downloadPath: Path
) : TorrentDownloader {

    override fun isFinished(sourceItem: SourceItem): Boolean {
        return true
    }

    override fun submit(task: DownloadTask): Boolean {
        val dp = task.downloadPath
        task.downloadFiles.filter { it.path.notExists() }
            .forEach {
                val resolve = dp.resolve(it.path)
                if (resolve.parent.notExists()) {
                    resolve.parent.createDirectories()
                }
                Files.createFile(resolve)
            }
        return true
    }

    override fun defaultDownloadPath(): Path {
        return downloadPath
    }

    override fun cancel(sourceItem: SourceItem, files: List<SourceFile>) {
        // Do nothing
    }

    override fun move(itemContent: ItemContent): Boolean {
        itemContent.sourceFiles
            .forEach {
                it.fileDownloadPath.moveTo(it.targetPath())
            }
        return true
    }

    override fun replace(itemContent: ItemContent): Boolean {
        itemContent.sourceFiles
            .forEach {
                it.fileDownloadPath.moveTo(it.targetPath(), true)
            }
        return true
    }

    override fun getPaths(torrentHash: String): List<Path> {
        return emptyList()
    }

}

