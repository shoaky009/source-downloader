package io.github.shoaky.sourcedownloader.integration.support

import io.github.shoaky.sourcedownloader.component.downloader.MockDownloader
import io.github.shoaky.sourcedownloader.sdk.DownloadTask
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.Downloader
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class DelayItemDownloader(
    private val downloadPath: Path
) : Downloader {

    private val downloader = MockDownloader(downloadPath)
    private val p: MutableMap<String, List<SourceFile>> = ConcurrentHashMap()
    private val cancelInvoked: MutableList<SourceItem> = Collections.synchronizedList(mutableListOf())

    override fun submit(task: DownloadTask): Boolean {
        val hashing = task.sourceItem.hashing()
        p[hashing] = task.downloadFiles
        if (task.sourceItem.title == "test1") {
            Thread.sleep(1000)
        }
        println("========== submit $hashing ${p.containsKey(hashing)}")
        if (p.containsKey(hashing)) {
            return downloader.submit(task)
        }
        return false
    }

    override fun defaultDownloadPath(): Path {
        return downloadPath
    }

    override fun cancel(sourceItem: SourceItem, files: List<SourceFile>) {
        println("========== cancel ${sourceItem.hashing()}")

        p.remove(sourceItem.hashing())
        downloader.cancel(sourceItem, files)
        cancelInvoked.add(sourceItem)
    }

    fun getCanceled(): List<SourceItem> {
        return cancelInvoked
    }
}