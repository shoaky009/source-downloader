package xyz.shoaky.sourcedownloader.core

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.component.Components
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.inputStream

class YamlConfigStorage(
    private val configPath: Path = Path("config.yaml")
) : ProcessorConfigStorage, ComponentConfigStorage,
    ConfigOperator, InstanceConfigStorage {

    override fun getAllProcessorConfig(): List<ProcessorConfig> {
        val get = yamlMapper.readTree(configPath.inputStream()).get("processors") ?: return emptyList()
        return yamlMapper.convertValue(get, jacksonTypeRef())
    }


    override fun getAllComponentConfig(): Map<String, List<ComponentConfig>> {
        val get = yamlMapper.readTree(configPath.inputStream()).get("components")
        return yamlMapper.convertValue(get, jacksonTypeRef())
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
    override fun deleteComponent(topType: Components, type: String, name: String) {
        TODO()
    }

    @Synchronized
    override fun deleteProcessor(name: String) {
        TODO()
    }

    companion object {
        private val yamlMapper = YAMLMapper()

        init {
            yamlMapper
                .registerModule(KotlinModule.Builder().build())
                .registerModule(JavaTimeModule())
            yamlMapper
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
                .enable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE)
                .enable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        }
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