package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.component.supplier.UrlDownloaderSupplier
import io.github.shoaky.sourcedownloader.sdk.DownloadTask
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sourceItem
import io.github.shoaky.sourcedownloader.testResourcePath
import org.junit.jupiter.api.Test
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists

class UrlDownloaderTest {

    private val savePath = Path("src", "test", "resources", "downloads")

    private val downloader = UrlDownloaderSupplier.apply(
        Properties.fromMap(
            mapOf(
                "download-path" to savePath
            )
        )
    )

    init {
        savePath.createDirectories()
    }

    @Test
    fun should_file_exists() {
        val sourceFilePath = testResourcePath.resolve("config.yaml")
        val uri = sourceFilePath.toUri().toString()
        val sourceItem = sourceItem("test", link = uri, downloadUrl = uri)
        val targetPath = savePath.resolve("config.yaml")
        targetPath.deleteIfExists()

        val task = DownloadTask(
            sourceItem,
            listOf(
                SourceFile(targetPath)
            ),
            downloadPath = savePath
        )
        downloader.submit(task)
        assert(targetPath.exists())

        targetPath.deleteIfExists()
    }
}