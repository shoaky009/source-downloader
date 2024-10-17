package io.github.shoaky.sourcedownloader.config

import io.github.shoaky.sourcedownloader.core.YamlConfigOperator
import io.github.shoaky.sourcedownloader.core.component.ConfigOperator
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import kotlin.io.path.Path
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.notExists

@Configuration
class StorageConfiguration(
    private val props: SourceDownloaderProperties
) {

    @Bean
    @ConditionalOnMissingBean(ConfigOperator::class)
    fun yamlConfigStorage(): ConfigOperator {
        val dataLocation = props.dataLocation
        val configPath = dataLocation.resolve("config.yaml")
        if (configPath.exists()) {
            return YamlConfigOperator(configPath)
        }

        val path = Path("core", "src", "main", "resources", "config.yaml")
        if (path.exists()) {
            return YamlConfigOperator(path)
        }
        val defaultPath = Path("config.yaml")
        if (defaultPath.notExists()) {
            defaultPath.createFile()
        }
        return YamlConfigOperator(defaultPath)
    }

}