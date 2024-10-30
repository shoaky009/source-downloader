package io.github.shoaky.sourcedownloader.core

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import io.github.shoaky.sourcedownloader.component.replacer.RegexVariableReplacer
import io.github.shoaky.sourcedownloader.core.component.ComponentConfig
import io.github.shoaky.sourcedownloader.core.component.ComponentId
import io.github.shoaky.sourcedownloader.core.component.ConfigOperator
import io.github.shoaky.sourcedownloader.core.expression.ExpressionType
import io.github.shoaky.sourcedownloader.core.file.VariableErrorStrategy
import io.github.shoaky.sourcedownloader.core.processor.VariableConflictStrategy
import io.github.shoaky.sourcedownloader.core.processor.VariableProcessOutput
import io.github.shoaky.sourcedownloader.sdk.DownloadOptions
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentTopType
import io.github.shoaky.sourcedownloader.util.jackson.yamlMapper
import org.slf4j.LoggerFactory
import org.yaml.snakeyaml.Yaml
import java.nio.file.Path
import java.time.Duration
import kotlin.io.path.*

@Suppress("UNCHECKED_CAST")
class YamlConfigOperator(
    private val configPath: Path = Path("config.yaml")
) : ConfigOperator {

    init {
        log.info("Config path: {}", configPath.toAbsolutePath())
    }

    fun init() {
        if (configPath.parent != null && configPath.parent.notExists()) {
            configPath.parent.createDirectories()
        }
        if (configPath.notExists()) {
            log.info("Config file not found, create a new one")
            val config = AllDeclaredConfig()
            yamlMapper.writeValue(configPath.toFile(), config)
        }
    }

    private val cache = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofSeconds(10))
        .build(object : CacheLoader<Path, AllDeclaredConfig>() {
            override fun load(path: Path): AllDeclaredConfig {
                // 为了纯粹的启动速度手动构建
                val yaml = Yaml()
                val root = yaml.load<Map<String, Any>>(path.readText())
                val instances = instanceConfigs(root)
                val components = componentsConfigs(root)
                val processors = processorsConfigs(root)
                return AllDeclaredConfig(instances = instances, components = components, processors = processors)
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
    override fun deleteComponent(topType: ComponentTopType, type: String, name: String): Boolean {
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

    private fun processorsConfigs(root: Map<String, Any>): MutableList<ProcessorConfig> {
        val processors = root.getOrDefault("processors", emptyList<Any>()) as List<Map<String, Any>>
        return processors.map { processor ->
            val triggers = (processor["triggers"] as List<String>? ?: emptyList()).map {
                ComponentId(it)
            }

            ProcessorConfig(
                processor["name"]?.toString() ?: throw IllegalArgumentException("Processor name is required"),
                triggers,
                processor["source"]?.toString()?.let { ComponentId(it) }
                    ?: throw IllegalArgumentException("Processor source is required"),
                processor["item-file-resolver"]?.toString()?.let { ComponentId(it) }
                    ?: throw IllegalArgumentException("Processor item-file-resolver is required"),
                processor["downloader"]?.toString()?.let { ComponentId(it) }
                    ?: throw IllegalArgumentException("Processor downloader is required"),
                processor["file-mover"]?.toString()?.let { ComponentId(it) }
                    ?: ComponentId("mover:general"),
                Path(
                    processor["save-path"]?.toString()
                        ?: throw IllegalArgumentException("Processor save-path is required")
                ),
                convertProcessorOptions(processor["options"] as Map<String, Any>?),
                processor["enabled"]?.toString()?.toBoolean() ?: true,
                processor["category"]?.toString(),
                (processor["tags"] as List<String>?)?.toSet() ?: emptySet()

            )
        }.toMutableList()
    }

    private fun convertProcessorOptions(options: Map<String, Any>?): ProcessorConfig.Options {
        if (options == null) {
            return ProcessorConfig.Options()
        }
        return ProcessorConfig.Options(
            savePathPattern = options["save-path-pattern"]?.toString() ?: "",
            filenamePattern = options["filename-pattern"]?.toString() ?: "",
            fileTaggers = (options["file-taggers"] as List<String>?)?.map { ComponentId(it) } ?: emptyList(),
            variableProviders = (options["variable-providers"] as List<String>?)?.map { ComponentId(it) }
                ?: emptyList(),
            regexVariableReplacers = (options["regex-variable-replacers"] as List<Map<String, Any>>?)?.map { prop ->
                RegexVariableReplacer(
                    prop["regex"]?.toString()?.let { Regex(it) } ?: throw IllegalArgumentException("Regex is required"),
                    prop["replacement"]?.toString() ?: throw IllegalArgumentException("Replacement is required")
                )
            } ?: emptyList(),
            variableReplacers = variableReplacers(options["variable-replacers"] as List<Map<String, Any>>?),
            variableNameReplace = (options["variable-name-replace"] as Map<String, String>?) ?: emptyMap(),
            variableErrorStrategy = VariableErrorStrategy.valueOf(
                options["variable-error-strategy"]?.toString() ?: VariableErrorStrategy.STAY.name
            ),
            variableConflictStrategy = VariableConflictStrategy.valueOf(
                options["variable-conflict-strategy"]?.toString() ?: VariableConflictStrategy.SMART.name
            ),
            fileExistsDetector = options["file-exists-detector"]?.toString()?.let { ComponentId(it) },
            fileReplacementDecider = options["file-replacement-decider"]?.toString()?.let { ComponentId(it) }
                ?: ComponentId("never"),
            fileGrouping = fileGrouping(options["file-grouping"] as List<Map<String, Any>>?),
            itemGrouping = itemGroupingConfigs(options["item-grouping"] as List<Map<String, Any>>?),
            supportWindowsPlatformPath = options["support-windows-platform-path"]?.toString()?.toBoolean() ?: true,
            variableProcess = variableProcess(options["variable-process"] as List<Map<String, Any>>?),
            itemFilters = (options["item-filters"] as List<String>?)?.map { ComponentId(it) } ?: emptyList(),
            fileFilters = (options["file-filters"] as List<String>?)?.map { ComponentId(it) } ?: emptyList(),
            itemContentFilters = (options["item-content-filters"] as List<String>?)?.map { ComponentId(it) }
                ?: emptyList(),
            itemExpressionExclusions = (options["item-expression-exclusions"] as List<String>?) ?: emptyList(),
            itemExpressionInclusions = (options["item-expression-inclusions"] as List<String>?) ?: emptyList(),
            contentExpressionExclusions = (options["content-expression-exclusions"] as List<String>?) ?: emptyList(),
            contentExpressionInclusions = (options["content-expression-inclusions"] as List<String>?) ?: emptyList(),
            fileExpressionExclusions = (options["file-expression-exclusions"] as List<String>?) ?: emptyList(),
            fileExpressionInclusions = (options["file-expression-inclusions"] as List<String>?) ?: emptyList(),
            // processListeners = (options["process-listeners"] as List<Any>?)?.map { prop ->
            //
            //     // ListenerConfig()
            // } ?: emptyList(),
            renameTaskInterval = options["rename-task-interval"]?.toString()?.let { Duration.parse(it) }
                ?: Duration.ofMinutes(1),
            renameTimesThreshold = options["rename-times-threshold"]?.toString()?.toInt() ?: 3,
            saveProcessingContent = options["save-processing-content"]?.toString()?.toBoolean() ?: true,
            fetchLimit = options["fetch-limit"]?.toString()?.toInt() ?: 50,
            pointerBatchMode = options["pointer-batch-mode"]?.toString()?.toBoolean() ?: true,
            itemErrorContinue = options["item-error-continue"]?.toString()?.toBoolean() ?: false,
            touchItemDirectory = options["touch-item-directory"]?.toString()?.toBoolean() ?: true,
            deleteEmptyDirectory = options["delete-empty-directory"]?.toString()?.toBoolean() ?: true,
            parallelism = options["parallelism"]?.toString()?.toInt() ?: 1,
            retryBackoffMills = options["retry-backoff-mills"]?.toString()?.toLong() ?: 5000L,
            taskGroup = options["task-group"]?.toString(),
            channelBufferSize = options["channel-buffer-size"]?.toString()?.toInt() ?: 20,
            downloadOptions = downloadOptions(options["download-options"] as Map<String, Any>?),
            manualSources = (options["manual-sources"] as List<String>?)?.map { ComponentId(it) } ?: emptyList(),
            expression = options["expression"]?.toString()?.let { ExpressionType.valueOf(it) }
                ?: ExpressionType.CEL
        )
    }

    private fun variableReplacers(raw: List<Map<String, Any>>?): List<VariableReplacerConfig> {
        if (raw == null) {
            return emptyList()
        }
        return raw.map { prop ->
            VariableReplacerConfig(
                ComponentId(
                    prop["id"]?.toString() ?: throw IllegalArgumentException("Variable replacer id is required")
                ),
                (prop["keys"] as List<String>?)?.toSet()
            )
        }
    }

    private fun variableProcess(raw: List<Map<String, Any>>?): List<ProcessorConfig.VariableProcessConfig> {
        if (raw == null) {
            return emptyList()
        }
        return raw.map { prop ->
            ProcessorConfig.VariableProcessConfig(
                prop["input"]?.toString() ?: throw IllegalArgumentException("Variable process input is required"),
                (prop["chain"] as List<String>?)?.map { ComponentId(it) } ?: emptyList(),
                variableProcessOutput(prop["output"] as Map<String, Any>?),
                prop["condition-expression"]?.toString()
            )
        }
    }

    private fun variableProcessOutput(raw: Map<String, Any>?): VariableProcessOutput {
        if (raw == null) {
            return VariableProcessOutput()
        }
        return VariableProcessOutput(
            (raw["key-mapping"] as Map<String, String>?) ?: emptyMap(),
            (raw["exclude-keys"] as List<String>?)?.toSet() ?: emptySet(),
            (raw["include-keys"] as List<String>?)?.toSet() ?: emptySet()
        )
    }

    private fun itemGroupingConfigs(options: List<Map<String, Any>>?): List<ProcessorConfig.ItemGroupingConfig> {
        if (options == null) {
            return emptyList()
        }
        return options.map { prop ->
            ProcessorConfig.ItemGroupingConfig(
                prop["tags"] as Set<String>? ?: emptySet(),
                prop["expression-matching"]?.toString(),
                prop["filename-pattern"]?.toString(),
                prop["save-path-pattern"]?.toString(),
                (prop["variable-providers"] as List<String>?)?.map { ComponentId(it) },
                (prop["item-filters"] as List<String>?)?.map { ComponentId(it) },
                prop["item-expression-exclusions"] as List<String>? ?: emptyList(),
                prop["item-expression-inclusions"] as List<String>? ?: emptyList(),
            )
        }
    }

    private fun fileGrouping(raw: List<Map<String, Any>>?): List<ProcessorConfig.FileGroupingConfig> {
        if (raw == null) {
            return emptyList()
        }
        return raw.map { prop ->
            ProcessorConfig.FileGroupingConfig(
                (prop["tags"] as List<String>?)?.toSet() ?: emptySet(),
                prop["expression-matching"]?.toString(),
                prop["filename-pattern"]?.toString(),
                prop["save-path-pattern"]?.toString(),
                (prop["file-content-filters"] as List<String>?)?.map { ComponentId(it) },
                prop["file-expression-exclusions"] as List<String>? ?: emptyList(),
                prop["file-expression-inclusions"] as List<String>? ?: emptyList(),
                prop["file-replacement-decider"]?.toString()?.let { ComponentId(it) }
            )
        }
    }

    private fun downloadOptions(raw: Map<String, Any>?): DownloadOptions {
        if (raw == null) {
            return DownloadOptions()
        }
        return DownloadOptions(
            raw["category"]?.toString(),
            raw["tags"] as List<String>? ?: emptyList(),
            raw["headers"] as Map<String, String>? ?: emptyMap()
        )
    }

    private fun componentsConfigs(root: Map<String, Any>): MutableMap<String, MutableList<ComponentConfig>> {
        val components = root.getOrDefault("components", emptyMap<String, List<Map<String, Any>>>())
            as Map<String, List<Map<String, Any>>>
        if (components.isEmpty()) {
            return mutableMapOf()
        }
        val componentTypes = ComponentTopType.entries.map { it.primaryName }
        val result: MutableMap<String, MutableList<ComponentConfig>> = mutableMapOf()
        for (componentType in componentTypes) {
            val typeComponents = components[componentType] ?: continue
            val list = typeComponents.map {
                ComponentConfig(
                    it["name"]?.toString() ?: throw IllegalArgumentException("Component name is required"),
                    it["type"]?.toString() ?: throw IllegalArgumentException("Component type is required"),
                    it["props"] as Map<String, Any>? ?: emptyMap()
                )
            }.toMutableList()
            result[componentType] = list
        }
        return result
    }

    private fun instanceConfigs(root: Map<String, Any>): MutableList<InstanceConfig> {
        return (root.getOrDefault("instances", emptyList<Any>()) as List<Map<String, Any>>)
            .map {
                InstanceConfig(
                    it["name"]?.toString() ?: throw IllegalArgumentException("Instance name is required"),
                    it["props"] as Map<String, Any>
                )
            }.toMutableList()
    }

    companion object {

        private val log = LoggerFactory.getLogger(YamlConfigOperator::class.java)
    }
}