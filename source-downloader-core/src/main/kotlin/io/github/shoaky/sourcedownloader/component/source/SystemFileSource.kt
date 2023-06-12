package io.github.shoaky.sourcedownloader.component.source

import io.github.shoaky.sourcedownloader.sdk.DownloadTask
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.Downloader
import io.github.shoaky.sourcedownloader.util.creationTime
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import kotlin.io.path.*

class SystemFileSource(
    private val path: Path,
    /**
     * 0: 把路径下的文件(包括文件夹 文件夹下的作为item下的子文件)作为一个SourceItem
     * 1: 把路径下的所有文件(不包括文件夹，包括子路径下的文件)作为一个SourceItem
     */
    private val mode: Int = 0
    // 用这个只是偷懒，如果要面对超大的文件数量需要用到pointer
) : io.github.shoaky.sourcedownloader.sdk.AlwaysLatestSource(), Downloader {

    override fun fetch(): Iterable<SourceItem> {
        return when (mode) {
            0 -> createRootFileSourceItems()
            1 -> createEachFileSourceItems()
            else -> throw RuntimeException("Unknown mode: $mode")
        }
    }

    @OptIn(ExperimentalPathApi::class)
    private fun createEachFileSourceItems(): List<SourceItem> {
        return path.walk()
            .filter { it.isHidden().not() }
            .filter { it.isDirectory().not() }
            .map {
                fromPath(it)
            }.toList()
    }

    private fun createRootFileSourceItems(): List<SourceItem> {
        return Files.list(path)
            .filter { it.isHidden().not() }
            .map {
                fromPath(it)
            }.toList()
    }

    private fun fromPath(it: Path): SourceItem {
        val creationTime = it.creationTime() ?: LocalDateTime.now()
        val url = it.toUri()
        val type = if (it.isDirectory()) "directory" else "system-file"
        return SourceItem(it.nameWithoutExtension, url, creationTime, type, url)
    }

    override fun submit(task: DownloadTask) {
        // Do nothing
    }

    override fun defaultDownloadPath(): Path {
        return path
    }


}

