package xyz.shoaky.sourcedownloader.component.downloader

import bt.metainfo.MetadataService
import org.springframework.core.io.UrlResource
import xyz.shoaky.sourcedownloader.sdk.DownloadTask
import xyz.shoaky.sourcedownloader.sdk.SourceContent
import xyz.shoaky.sourcedownloader.sdk.SourceItem
import xyz.shoaky.sourcedownloader.sdk.component.TorrentDownloader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.moveTo
import kotlin.io.path.notExists

class MockDownloader(private val downloadPath: Path) : TorrentDownloader {

    private val metadataService = MetadataService()

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

    override fun resolveFiles(sourceItem: SourceItem): List<Path> {
        val contentType = sourceItem.contentType
        val downloadUrl = sourceItem.downloadUri
        if (contentType.contains("torrent")) {
            val torrent = metadataService.fromUrl(downloadUrl.toURL())
            if (torrent.files.size == 1) {
                return torrent.files.map { it.pathElements.joinToString("/") }
                    .map { Path(it) }
            }
            val parent = Path(torrent.name)
            return torrent.files
                .filter { it.size > 0 }
                .map { it.pathElements.joinToString("/") }
                .map { parent.resolve(it) }
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

