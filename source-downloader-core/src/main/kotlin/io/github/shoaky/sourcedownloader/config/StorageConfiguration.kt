package io.github.shoaky.sourcedownloader.config

import io.github.shoaky.sourcedownloader.core.YamlConfigStorage
import io.github.shoaky.sourcedownloader.core.component.ConfigOperator
import io.github.shoaky.sourcedownloader.core.component.DefaultInstanceManager
import io.github.shoaky.sourcedownloader.core.component.InstanceConfigStorage
import io.github.shoaky.sourcedownloader.sdk.InstanceManager
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
            return YamlConfigStorage(configPath)
        }

        val path = Path("source-downloader-core", "src", "main", "resources", "config.yaml")
        if (path.exists()) {
            return YamlConfigStorage(path)
        }
        val defaultPath = Path("config.yaml")
        if (defaultPath.notExists()) {
            defaultPath.createFile()
        }
        return YamlConfigStorage(defaultPath)
    }

    @Bean
    fun instanceManager(storage: InstanceConfigStorage): InstanceManager {
        return DefaultInstanceManager(storage)
    }
}