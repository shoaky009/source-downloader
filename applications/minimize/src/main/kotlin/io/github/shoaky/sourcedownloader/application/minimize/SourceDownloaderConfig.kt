package io.github.shoaky.sourcedownloader.application.minimize

import java.nio.file.Path
import kotlin.io.path.Path

data class SourceDownloaderConfig(
    val dataLocation: Path = run {
        val path = System.getenv("SOURCE_DOWNLOADER_DATA_LOCATION") ?: ""
        Path(path)
    }
)