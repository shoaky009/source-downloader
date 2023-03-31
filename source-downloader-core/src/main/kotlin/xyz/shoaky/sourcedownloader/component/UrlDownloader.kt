package xyz.shoaky.sourcedownloader.component

import org.springframework.core.io.UrlResource
import xyz.shoaky.sourcedownloader.sdk.DownloadTask
import xyz.shoaky.sourcedownloader.sdk.SourceItem
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.Downloader
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier
import java.io.FileOutputStream
import java.nio.channels.Channels
import java.nio.file.Path
import kotlin.io.path.Path

class UrlDownloader(private val downloadPath: Path) : Downloader {

    override fun submit(task: DownloadTask) {
        val uriResource = UrlResource(task.downloadUri())
        val filename = uriResource.filename.takeIf { it.isNullOrBlank().not() }
            ?: task.sourceItem.hashing()
        val dp = task.downloadPath ?: downloadPath

        val targetPath = dp.resolve(filename)
        val readableByteChannel = Channels.newChannel(uriResource.inputStream)
        FileOutputStream(targetPath.toFile()).use {
            it.channel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE)
        }
    }

    override fun defaultDownloadPath(): Path {
        return downloadPath
    }

    override fun resolveFiles(sourceItem: SourceItem): List<Path> {
        val filename = UrlResource(sourceItem.downloadUri).filename
            ?: sourceItem.hashing()
        return listOf(Path(filename))
    }

}

object UrlDownloaderSupplier : SdComponentSupplier<UrlDownloader> {
    override fun apply(props: ComponentProps): UrlDownloader {
        val path = props.properties["download-path"]?.let {
            Path(it.toString())
        } ?: throw RuntimeException("download-path is null")
        return UrlDownloader(path)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.downloader("url"),
        )
    }

    override fun getComponentClass(): Class<UrlDownloader> {
        return UrlDownloader::class.java
    }
}