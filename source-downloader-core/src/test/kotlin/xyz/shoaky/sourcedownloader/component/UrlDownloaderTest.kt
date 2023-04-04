package xyz.shoaky.sourcedownloader.component

import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.sdk.DownloadTask
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sourceItem
import kotlin.io.path.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists

class UrlDownloaderTest {

    private val savePath = Path("src/test/resources/downloads")
    private val targetPath = savePath.resolve("0f9a2d944ea7c4952e274dd1738005e4e18107b3832b6d6cf9d74933f123bdb3")
    private val downloader = UrlDownloaderSupplier.apply(
        ComponentProps.fromMap(
            mapOf(
                "download-path" to savePath
            ))
    )

    @Test
    fun should_file_exists() {
        targetPath.deleteIfExists()

        val url = "https://www.baidu.com"
        val task = DownloadTask.create(sourceItem("test", link = url, downloadUrl = "https://www.baidu.com"),
            listOf(
                targetPath
            ))
        downloader.submit(task)
        assert(targetPath.exists())

        targetPath.deleteIfExists()
    }
}