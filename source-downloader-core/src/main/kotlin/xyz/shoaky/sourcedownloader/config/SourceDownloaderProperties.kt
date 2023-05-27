package xyz.shoaky.sourcedownloader.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.nio.file.Path

@ConfigurationProperties(prefix = "source-downloader")
data class SourceDownloaderProperties(
    val dataLocation: Path
)