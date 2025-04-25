package io.github.shoaky.sourcedownloader.core

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import io.github.shoaky.sourcedownloader.core.component.ComponentConfig
import io.github.shoaky.sourcedownloader.core.component.ConfigOperator
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentRootType
import io.github.shoaky.sourcedownloader.util.jackson.yamlMapper
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.time.Duration
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.inputStream
import kotlin.io.path.notExists

class YamlConfigOperator(
    private val configPath: Path = Path("config.yaml")
) : ConfigOperator {

    fun init() {
        log.info("Config path: {}", configPath.toAbsolutePath())
        if (configPath.parent != null && configPath.parent.notExists()) {
            configPath.parent.createDirectories()
        }
        if (configPath.notExists()) {
            log.info("Config file not found, create a new one")
            val config = AllDeclaredConfig()
            yamlMapper.writeValue(configPath.toFile(), config)
        }
    }

    private val cache = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofSeconds(5))
        .build(object : CacheLoader<Path, AllDeclaredConfig>() {
            override fun load(path: Path): AllDeclaredConfig {
                return yamlMapper.readValue(configPath.inputStream(), AllDeclaredConfig::class.java)
            }
        })

    override fun getAllProcessorConfig(): List<ProcessorConfig> {
        return cache.get(configPath).processors
    }

    override fun getAllComponentConfig(): Map<String, List<ComponentConfig>> {
        return cache.get(configPath).components
    }

    @Synchronized
    override fun save(type: String, componentConfig: ComponentConfig) {
        val config = yamlMapper.readValue(configPath.inputStream(), AllDeclaredConfig::class.java)
        val typeConfigs = config.components[type] ?: mutableListOf()
        val index = typeConfigs.indexOfFirst { it.name == componentConfig.name }
        if (index < 0) {
            typeConfigs.add(componentConfig)
        } else {
            typeConfigs[index] = componentConfig
        }
        yamlMapper.writeValue(configPath.toFile(), config)
        cache.invalidateAll()
    }

    @Synchronized
    override fun save(name: String, processorConfig: ProcessorConfig) {
        val config = yamlMapper.readValue(configPath.inputStream(), AllDeclaredConfig::class.java)
        val processors = config.processors
        val index = processors.indexOfFirst { it.name == name }
        if (index < 0) {
            processors.add(processorConfig)
        } else {
            processors[index] = processorConfig
        }
        yamlMapper.writeValue(configPath.toFile(), config)
        cache.invalidateAll()
    }

    @Synchronized
    override fun deleteComponent(topType: ComponentRootType, type: String, name: String): Boolean {
        val config = yamlMapper.readValue(configPath.inputStream(), AllDeclaredConfig::class.java)
        val components = config.components
        val configs = components[topType.primaryName] ?: mutableListOf()
        val removed = configs.removeIf { it.type == type && it.name == name }
        if (removed) {
            yamlMapper.writeValue(configPath.toFile(), config)
        }
        cache.invalidateAll()
        return removed
    }

    @Synchronized
    override fun deleteProcessor(name: String): Boolean {
        val config = yamlMapper.readValue(configPath.inputStream(), AllDeclaredConfig::class.java)
        val processors = config.processors
        val removed = processors.removeIf { it.name == name }
        if (removed) {
            yamlMapper.writeValue(configPath.toFile(), config)
        }
        cache.invalidateAll()
        return removed
    }

    override fun getInstanceProps(name: String): Properties {
        val config = cache.get(configPath)
        val typeConfigs = config.instances
        val props = typeConfigs.filter { it.name == name }
            .map {
                Properties.fromMap(it.props)
            }.firstOrNull() ?: throw IllegalArgumentException("instance $name not found")
        return props
    }

    companion object {

        private val log = LoggerFactory.getLogger(YamlConfigOperator::class.java)
    }
}