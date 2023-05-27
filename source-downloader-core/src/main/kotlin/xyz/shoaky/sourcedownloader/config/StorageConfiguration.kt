package xyz.shoaky.sourcedownloader.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import xyz.shoaky.sourcedownloader.core.DefaultInstanceManager
import xyz.shoaky.sourcedownloader.core.InstanceConfigStorage
import xyz.shoaky.sourcedownloader.core.YamlConfigStorage
import xyz.shoaky.sourcedownloader.sdk.InstanceManager
import kotlin.io.path.Path
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.notExists

@Configuration
class StorageConfiguration(
    private val props: SourceDownloaderProperties
) {

    @Bean
    fun yamlConfigStorage(): YamlConfigStorage {
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