package io.github.shoaky.sourcedownloader.application.vertx

import com.zaxxer.hikari.HikariConfig
import io.vertx.core.http.HttpServerOptions
import java.nio.file.Path
import kotlin.io.path.Path

data class ApplicationConfig(
    val server: HttpServerOptions = HttpServerOptions().also {
        it.port = 8080
    },
    val sourceDownloader: SourceDownloaderConfig = SourceDownloaderConfig(),
    val datasource: HikariConfig = HikariConfig()
) {

    init {
        val path = sourceDownloader.dataLocation.toString().ifBlank { "." }
        datasource
            .also {
                it.driverClassName = "org.sqlite.JDBC"
                it.jdbcUrl = "jdbc:sqlite:$path/source-downloader.db"
                it.username = "sd"
                it.password = "sd"
            }
    }
}

data class SourceDownloaderConfig(
    val dataLocation: Path = run {
        val path = System.getenv("SOURCE_DOWNLOADER_DATA_LOCATION") ?: ""
        Path(path)
    }
)