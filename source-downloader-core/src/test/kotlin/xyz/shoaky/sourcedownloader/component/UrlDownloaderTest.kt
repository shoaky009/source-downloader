package xyz.shoaky.sourcedownloader.component

import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.component.supplier.UrlDownloaderSupplier
import xyz.shoaky.sourcedownloader.sdk.DownloadTask
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sourceItem
import xyz.shoaky.sourcedownloader.testResourcePath
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
                targetPath
            ),
            downloadPath = savePath
        )
        downloader.submit(task)
        assert(targetPath.exists())

        targetPath.deleteIfExists()
    }
}