package xyz.shoaky.sourcedownloader.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "source-downloader")
data class SourceDownloaderProperties(
    val dataLocation: String
)