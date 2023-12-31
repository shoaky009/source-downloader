package io.github.shoaky.sourcedownloader.foreign.methods

data class DownloaderForeignMethods(
    val submit: String = "/downloader/submit",
    val cancel: String = "/downloader/cancel",
    val defaultDownloadPath: String = "/downloader/default_download_path",
)