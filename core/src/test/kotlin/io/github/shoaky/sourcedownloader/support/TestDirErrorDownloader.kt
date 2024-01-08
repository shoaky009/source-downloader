package io.github.shoaky.sourcedownloader.support

import io.github.shoaky.sourcedownloader.sdk.DownloadTask
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.Downloader
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.createDirectories
import kotlin.io.path.notExists

class TestDirErrorDownloader(
    private val downloadPath: Path
) : Downloader {

    private val counting = AtomicInteger()

    override fun submit(task: DownloadTask): Boolean {
        if (task.sourceItem.title != "test-dir") {
            createFiles(task)
            return true
        }

        if (counting.getAndIncrement() < 1) {
            throw RuntimeException("Just A Test")
        }

        createFiles(task)
        return true
    }

    private fun createFiles(task: DownloadTask) {
        task.downloadFiles.filter { it.path.notExists() }
            .forEach {
                val resolve = task.downloadPath.resolve(it.path)
                if (resolve.parent.notExists()) {
                    resolve.parent.createDirectories()
                }
                Files.createFile(resolve)
            }
    }

    override fun defaultDownloadPath(): Path {
        return downloadPath
    }

    override fun cancel(sourceItem: SourceItem, files: List<SourceFile>) {

    }
}