package io.github.shoaky.sourcedownloader.application.vertx

import com.zaxxer.hikari.HikariConfig
import io.vertx.core.http.HttpServerOptions

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