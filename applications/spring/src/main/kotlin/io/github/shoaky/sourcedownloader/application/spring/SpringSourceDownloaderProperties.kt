package io.github.shoaky.sourcedownloader.application.spring

import org.springframework.boot.context.properties.ConfigurationProperties
import java.nio.file.Path

@ConfigurationProperties(prefix = "source-downloader")
class SpringSourceDownloaderProperties(
    val dataLocation: Path
)