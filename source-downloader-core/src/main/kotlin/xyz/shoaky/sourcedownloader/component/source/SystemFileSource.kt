package xyz.shoaky.sourcedownloader.component.source

import xyz.shoaky.sourcedownloader.sdk.DownloadTask
import xyz.shoaky.sourcedownloader.sdk.SourceItem
import xyz.shoaky.sourcedownloader.sdk.component.Downloader
import xyz.shoaky.sourcedownloader.sdk.component.Source
import xyz.shoaky.sourcedownloader.util.creationTime
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
) : Source, Downloader {

    override fun fetch(): List<SourceItem> {
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

    override fun resolveFiles(sourceItem: SourceItem): List<Path> {
        val path = sourceItem.downloadUri.toPath()
        if (path.isDirectory()) {
            return Files.list(path).sorted().toList()
        }
        return listOf(path)
    }

}

