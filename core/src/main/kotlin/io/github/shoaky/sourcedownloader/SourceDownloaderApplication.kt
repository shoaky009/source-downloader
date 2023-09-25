package io.github.shoaky.sourcedownloader

import io.github.shoaky.sourcedownloader.SourceDownloaderApplication.Companion.log
import io.github.shoaky.sourcedownloader.component.supplier.*
import io.github.shoaky.sourcedownloader.config.SourceDownloaderProperties
import io.github.shoaky.sourcedownloader.core.PluginManager
import io.github.shoaky.sourcedownloader.core.ProcessorConfigStorage
import io.github.shoaky.sourcedownloader.core.component.ComponentConfig
import io.github.shoaky.sourcedownloader.core.component.ComponentConfigStorage
import io.github.shoaky.sourcedownloader.core.component.ComponentManager
import io.github.shoaky.sourcedownloader.core.component.DefaultInstanceManager
import io.github.shoaky.sourcedownloader.core.processor.ProcessorManager
import io.github.shoaky.sourcedownloader.sdk.InstanceManager
import io.github.shoaky.sourcedownloader.sdk.component.ComponentException
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentTopType
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.util.getObjectSuppliers
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.event.EventListener
import org.springframework.core.env.Environment
import java.net.URI

@SpringBootApplication
@EnableConfigurationProperties(SourceDownloaderProperties::class)
class SourceDownloaderApplication(
    private val environment: Environment,
    private val instanceManager: InstanceManager,
    private val componentManager: ComponentManager,
    private val processorManager: ProcessorManager,
    private val pluginManager: PluginManager,
    private val processorStorages: List<ProcessorConfigStorage>,
    private val componentStorages: List<ComponentConfigStorage>,
    private val componentSupplier: List<ComponentSupplier<*>>
) : InitializingBean {

    @EventListener(ApplicationReadyEvent::class)
    fun createProcessors() {
        log.info(
            "Database file located:${
                environment.getProperty("spring.datasource.url")
                    ?.removePrefix("jdbc:sqlite:")
            }"
        )

        val processorConfigs = processorStorages.flatMap { it.getAllProcessorConfig() }
        for (processorConfig in processorConfigs) {
            try {
                processorManager.createProcessor(processorConfig)
            } catch (e: Exception) {
                log.error("创建处理器失败:${processorConfig.name}")
                throw e
            }
        }
        componentManager.getAllTrigger()
            .forEach {
                it.start()
            }
    }

    @PreDestroy
    fun stopApplication() {
        pluginManager.destroyPlugins()
        componentManager.getAllTrigger()
            .forEach {
                it.stop()
            }
        destroyAllProcessor()
        destroyAllComponent()
        destroyAllInstance()
    }

    private fun loadAndInitPlugins() {
        pluginManager.loadPlugins()
        pluginManager.initPlugins()
    }

    private fun registerComponentSuppliers() {
        componentManager.registerSupplier(
            *componentSupplier.toTypedArray()
        )
        componentManager.registerSupplier(
            *getObjectSuppliers(
                "io.github.shoaky.sourcedownloader.component.supplier",
            )
            // *getObjectSuppliers0()
        )
        val types = componentManager.getSuppliers()
            .map { it.supplyTypes() }
            .flatten()
            .distinct()
            .groupBy({ it.topTypeClass.simpleName }, { it.typeName })
        log.info("Component supplier registration completed:$types")
    }

    private fun createComponents() {
        componentManager.getSuppliers().filter {
            it.autoCreateDefault()
        }.forEach {
            for (type in it.supplyTypes()) {
                val typeName = type.typeName
                componentManager.createComponent(type.topType, ComponentConfig(
                    typeName,
                    typeName,
                ))
                log.info("Successfully created component ${type.topTypeClass.simpleName}:${typeName}:${typeName}")
            }
        }

        for (componentStorage in componentStorages) {
            componentStorage
                .getAllComponentConfig()
                .forEach(this::createFromConfigs)
        }
    }

    private fun createFromConfigs(key: String, configs: List<ComponentConfig>) {
        val type = ComponentTopType.fromName(key)
            ?: throw ComponentException.unsupported("未知组件类型:$key")

        configs
            .forEach {
                try {
                    componentManager.createComponent(type, it)
                } catch (e: ComponentException) {
                    log.error("Failed to create component ${type.klass.simpleName}:${it.type}:${it.name}, reason:${e.message}")
                    throw e
                }
                log.info("Successfully created component ${type.klass.simpleName}:${it.type}:${it.name}")
            }
    }

    override fun afterPropertiesSet() {
        // 加载出现异常不让应用完成启动
        loadAndInitPlugins()
        registerInstanceFactories()
        log.info("Supported component types:${ComponentType.types()}")
        registerComponentSuppliers()
        createComponents()
    }

    private fun registerInstanceFactories() {
        // Currently no instance factories are registered
    }

    fun reload() {
        destroyAllProcessor()
        destroyAllComponent()
        destroyAllInstance()
        createComponents()
        createProcessors()
    }

    private fun destroyAllInstance() {
        if (instanceManager is DefaultInstanceManager) {
            instanceManager.destroyAll()
        }
        log.info("All instances destroyed")
    }

    private fun destroyAllComponent() {
        val componentNames = componentManager.getAllComponentNames()
        for (name in componentNames) {
            componentManager.destroy(name)
        }
        log.info("All components destroyed")
    }

    private fun destroyAllProcessor() {
        val processorNames = processorManager.getAllProcessorNames()
        for (name in processorNames) {
            processorManager.destroyProcessor(name)
        }
        log.info("All processors destroyed")
    }

    companion object {

        internal val log = LoggerFactory.getLogger(SourceDownloaderApplication::class.java)

        private fun getObjectSuppliers0(): Array<ComponentSupplier<*>> {
            return arrayOf(
                AlwaysReplaceSupplier,
                DeleteEmptyDirectorySupplier,
                ExpressionFileFilterSupplier,
                ExpressionItemContentFilterSupplier,
                ExpressionItemFilterSupplier,
                FileSizeReplacementDeciderSupplier,
                FixedScheduleTriggerSupplier,
                FixedSourceSupplier,
                GeneralFileMoverSupplier,
                HardlinkFileMoverSupplier,
                ItemDirectoryExistsDetectorSupplier,
                MappedFileTaggerSupplier,
                MockDownloaderSupplier,
                NeverReplaceSupplier,
                NoneDownloaderSupplier,
                RunCommandSupplier,
                SendHttpRequestSupplier,
                SequenceVariableProviderSupplier,
                SystemFileResolverSupplier,
                SystemFileSourceSupplier,
                TouchItemDirectorySupplier,
                UriSourceSupplier,
                UrlDownloaderSupplier,
                UrlFileResolverSupplier,
                HttpDownloaderSupplier
            )
        }

        @JvmStatic
        fun main(args: Array<String>) {
            setupProxy()

            val springApplication = SpringApplication(SourceDownloaderApplication::class.java)
            springApplication.mainApplicationClass = SourceDownloaderApplication::class.java
            springApplication.run(*args)
        }

    }
}

/**
 * TODO 后面去掉兼容，AOT时用
 */
fun main(args: Array<String>) {
    setupProxy()
    SpringApplication.run(SourceDownloaderApplication::class.java, *args)
}

private fun setupProxy() {
    val env = System.getenv()
    val urlStr = env["http_proxy"] ?: env["HTTP_PROXY"] ?: env["https_proxy"] ?: env["HTTPS_PROXY"]
    urlStr?.also {
        val uri = URI(it)
        log.info("Proxy using:$uri")
        System.setProperty("http.proxyHost", uri.host)
        System.setProperty("http.proxyPort", uri.port.toString())
        System.setProperty("https.proxyHost", uri.host)
        System.setProperty("https.proxyPort", uri.port.toString())
    }
}