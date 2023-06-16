package io.github.shoaky.sourcedownloader.integration

import io.github.shoaky.sourcedownloader.config.SourceDownloaderProperties
import io.github.shoaky.sourcedownloader.core.YamlConfigStorage
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TestConfiguration(
    private val props: SourceDownloaderProperties
) {
    @Bean
    fun configOperator(): RestorableConfigOperator {
        val path = props.dataLocation.resolve("config.yaml")
        return RestorableConfigOperator(path, YamlConfigStorage(path))
    }

}