package io.github.shoaky.sourcedownloader.config

import org.jetbrains.exposed.spring.autoconfigure.ExposedAutoConfiguration
import org.jetbrains.exposed.sql.DatabaseConfig
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Import(value = [ExposedAutoConfiguration::class])
@EnableAutoConfiguration(exclude = [DataSourceTransactionManagerAutoConfiguration::class])
@Configuration
private class ExposedConfiguration {

    @Bean
    fun databaseConfig() = DatabaseConfig {
    }
}