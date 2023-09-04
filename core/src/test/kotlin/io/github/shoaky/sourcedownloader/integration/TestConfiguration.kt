package io.github.shoaky.sourcedownloader.integration

import io.github.shoaky.sourcedownloader.config.SourceDownloaderProperties
import io.github.shoaky.sourcedownloader.core.YamlConfigOperator
import org.springframework.boot.autoconfigure.flyway.FlywayDataSource
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.SingleConnectionDataSource
import javax.sql.DataSource

@Configuration
class TestConfiguration(
    private val props: SourceDownloaderProperties
) {

    @Bean
    fun configOperator(): RestorableConfigOperator {
        val path = props.dataLocation.resolve("config.yaml")
        return RestorableConfigOperator(path, YamlConfigOperator(path))
    }

    @Bean
    @FlywayDataSource
    fun dataSource(props: DataSourceProperties): DataSource {
        val dataSource = SingleConnectionDataSource(props.url, true)
        dataSource.setDriverClassName(props.driverClassName)
        dataSource.password = props.password
        dataSource.username = props.username
        return dataSource
    }

}