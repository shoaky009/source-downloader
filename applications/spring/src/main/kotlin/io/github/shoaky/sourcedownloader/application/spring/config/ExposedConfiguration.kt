package io.github.shoaky.sourcedownloader.application.spring.config

import org.jetbrains.exposed.v1.core.DatabaseConfig
import org.jetbrains.exposed.v1.spring.boot4.autoconfigure.ExposedAutoConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration
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