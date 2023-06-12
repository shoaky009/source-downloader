package io.github.shoaky.sourcedownloader.core

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentTopType
import io.github.shoaky.sourcedownloader.util.jackson.yamlMapper
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.inputStream

class YamlConfigStorage(
    private val configPath: Path = Path("config.yaml")
) : ProcessorConfigStorage, ComponentConfigStorage,
    ConfigOperator, InstanceConfigStorage {

    override fun getAllProcessorConfig(): List<ProcessorConfig> {
        val config = yamlMapper.readTree(configPath.inputStream()).get("processors") ?: return emptyList()
        return yamlMapper.convertValue(config, jacksonTypeRef())
    }


    override fun getAllComponentConfig(): Map<String, List<ComponentConfig>> {
        val config = yamlMapper.readTree(configPath.inputStream()).get("components") ?: return emptyMap()
        return yamlMapper.convertValue(config, jacksonTypeRef())
    }

    @Synchronized
    override fun save(type: String, componentConfig: ComponentConfig) {
        val config = yamlMapper.readValue(configPath.inputStream(), Config::class.java)
        val typeConfigs = config.components[type] ?: mutableListOf()
        val index = typeConfigs.indexOfFirst { it.name == componentConfig.name }
        if (index < 0) {
            typeConfigs.add(componentConfig)
        } else {
            typeConfigs[index] = componentConfig
        }
        yamlMapper.writeValue(configPath.toFile(), config)
    }

    @Synchronized
    override fun save(name: String, processorConfig: ProcessorConfig) {
        val config = yamlMapper.readValue(configPath.inputStream(), Config::class.java)
        val processors = config.processors
        val index = processors.indexOfFirst { it.name == name }
        if (index < 0) {
            processors.add(processorConfig)
        } else {
            processors[index] = processorConfig
        }
        yamlMapper.writeValue(configPath.toFile(), config)
    }

    @Synchronized
    override fun deleteComponent(topType: ComponentTopType, type: String, name: String) {
        TODO()
    }

    @Synchronized
    override fun deleteProcessor(name: String) {
        TODO()
    }

    override fun getInstanceProps(name: String): Properties {
        val config = yamlMapper.readValue(configPath.inputStream(), Config::class.java)
        val typeConfigs = config.instances
        val props = typeConfigs.filter { it.name == name }
            .map {
                Properties.fromMap(it.props)
            }.firstOrNull() ?: throw IllegalArgumentException("instance $name not found")
        return props
    }
}

data class InstanceConfig(
    val name: String,
    val props: Map<String, Any>
)

private data class Config(
    val components: MutableMap<String, MutableList<ComponentConfig>> = mutableMapOf(),
    val processors: MutableList<ProcessorConfig> = mutableListOf(),
    val instances: MutableList<InstanceConfig> = mutableListOf()
)