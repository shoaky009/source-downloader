package io.github.shoaky.sourcedownloader.application.vertx

import com.zaxxer.hikari.HikariConfig
import io.vertx.core.http.HttpServerOptions
import java.nio.file.Path
import kotlin.io.path.Path

data class ApplicationConfig(
    val server: HttpServerOptions = HttpServerOptions(),
    val sourceDownloader: SourceDownloaderConfig = SourceDownloaderConfig(),
    val datasource: HikariConfig = HikariConfig()
) {

    init {
        datasource
            .also {
                it.driverClassName = "org.sqlite.JDBC"
                it.jdbcUrl = "jdbc:sqlite:${sourceDownloader.dataLocation}source-downloader.db"
                it.username = "sd"
                it.password = "sd"
            }
    }
}

data class SourceDownloaderConfig(
    val dataLocation: Path = Path("")
)