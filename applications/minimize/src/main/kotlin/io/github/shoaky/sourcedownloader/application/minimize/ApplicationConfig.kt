package io.github.shoaky.sourcedownloader.application.minimize

data class ApplicationConfig(
    val port: Int,
    val sourceDownloader: SourceDownloaderConfig,
)