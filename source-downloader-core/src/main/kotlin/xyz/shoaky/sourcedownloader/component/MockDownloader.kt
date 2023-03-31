package xyz.shoaky.sourcedownloader.component

import bt.metainfo.MetadataService
import org.springframework.core.io.UrlResource
import xyz.shoaky.sourcedownloader.sdk.DownloadTask
import xyz.shoaky.sourcedownloader.sdk.SourceContent
import xyz.shoaky.sourcedownloader.sdk.SourceItem
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier
import xyz.shoaky.sourcedownloader.sdk.component.TorrentDownloader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.moveTo
import kotlin.io.path.name
import kotlin.io.path.notExists

class MockDownloader(private val downloadPath: Path) : TorrentDownloader {

    private val metadataService = MetadataService()

    override fun isFinished(task: DownloadTask): Boolean {
        return true
    }

    override fun submit(task: DownloadTask) {
        val last = resolveFiles(task.sourceItem).first()
        val path = task.downloadPath ?: downloadPath
        val resolve = path.resolve(last.name)
        if (resolve.notExists()) {
            Files.createFile(resolve)
        }
    }

    override fun defaultDownloadPath(): Path {
        return downloadPath
    }

    override fun resolveFiles(sourceItem: SourceItem): List<Path> {
        val contentType = sourceItem.contentType
        val downloadUrl = sourceItem.downloadUri
        if (contentType.contains("torrent")) {
            return metadataService.fromUrl(downloadUrl.toURL()).files
                .filter { it.size > 0 }
                .map { it.pathElements.joinToString("/") }
                .map { Path(it) }
        }

        val filename = UrlResource(downloadUrl).filename.takeIf { it.isNullOrBlank().not() }
            ?: sourceItem.hashing()
        return listOf(Path(filename))
    }

    override fun rename(sourceContent: SourceContent): Boolean {
        val sourceFiles = sourceContent.sourceFiles
        for (sourceFile in sourceFiles) {
            sourceFile.fileDownloadPath.moveTo(sourceFile.targetPath())
        }
        return true
    }

}

object MockDownloaderSupplier : SdComponentSupplier<MockDownloader> {
    override fun apply(props: ComponentProps): MockDownloader {
        val path = props.properties["download-path"]?.let {
            Path(it.toString())
        } ?: throw RuntimeException("download-path is null")
        return MockDownloader(path)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.downloader("mock")
        )
    }

    override fun getComponentClass(): Class<MockDownloader> {
        return MockDownloader::class.java
    }

}