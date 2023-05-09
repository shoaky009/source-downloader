package xyz.shoaky.sourcedownloader.component.downloader

import xyz.shoaky.sourcedownloader.sdk.DownloadTask
import xyz.shoaky.sourcedownloader.sdk.SourceContent
import xyz.shoaky.sourcedownloader.sdk.component.TorrentDownloader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.moveTo
import kotlin.io.path.notExists

class MockDownloader(private val downloadPath: Path) : TorrentDownloader {

    override fun isFinished(task: DownloadTask): Boolean {
        return true
    }

    override fun submit(task: DownloadTask) {
        val dp = task.downloadPath
        task.downloadFiles.filter { it.notExists() }
            .forEach {
                val resolve = dp.resolve(it)
                if (resolve.parent != dp && resolve.parent.notExists()) {
                    resolve.parent.createDirectories()
                }
                Files.createFile(resolve)
            }
    }

    override fun defaultDownloadPath(): Path {
        return downloadPath
    }

    override fun rename(sourceContent: SourceContent): Boolean {
        val sourceFiles = sourceContent.sourceFiles
        for (sourceFile in sourceFiles) {
            sourceFile.fileDownloadPath.moveTo(sourceFile.targetPath())
        }
        return true
    }

}

