package xyz.shoaky.sourcedownloader.core.component

import xyz.shoaky.sourcedownloader.sdk.DownloadTask
import xyz.shoaky.sourcedownloader.sdk.SourceItem
import xyz.shoaky.sourcedownloader.sdk.component.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.io.path.*

class SystemFileSource(
    private val path: Path,
    /**
     * 0: 把路径下的文件(包括文件夹 文件夹下的作为item下的子文件)作为一个SourceItem
     * 1: 把路径下的所有文件(不包括文件夹，包括子路径下的文件)作为一个SourceItem
     */
    private val mode: Int = 1
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
        val attrs = it.readAttributes<BasicFileAttributes>()
        val creationTime = attrs.creationTime()
        val dateTime = LocalDateTime.ofInstant(creationTime.toInstant(), ZoneId.systemDefault())
        val url = it.toUri().toURL()
        val type = if (it.isDirectory()) "directory" else "system-file"
        return SourceItem(it.nameWithoutExtension, url, dateTime, type, url)
    }

    override fun submit(task: DownloadTask) {
        // Do nothing
    }

    override fun defaultDownloadPath(): Path {
        return path
    }

    override fun resolveFiles(sourceItem: SourceItem): List<Path> {
        val path = sourceItem.downloadUrl.toURI().toPath()
        if (path.isDirectory()) {
            return Files.list(path).toList()
        }
        return listOf(path)
    }

}

object SystemFileSourceSupplier : SdComponentSupplier<SystemFileSource> {
    override fun apply(props: ComponentProps): SystemFileSource {
        return SystemFileSource(props.get("path"),
            props.getOrDefault("mode", 0)
        )
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.source("system-file"),
            ComponentType.downloader("system-file"),
        )
    }

    override fun getComponentClass(): Class<SystemFileSource> {
        return SystemFileSource::class.java
    }

    override fun rules(): List<ComponentRule> {
        return listOf(
            ComponentRule.allowSource(SystemFileSource::class),
            ComponentRule.allowDownloader(SystemFileSource::class),
            ComponentRule.allowMover(GeneralFileMover::class),
        )
    }
}